package com.riskcare.simulator;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.lang.IgniteCallable;
import org.apache.ignite.lang.IgniteReducer;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.time.temporal.ChronoUnit.DAYS;

public class App 
{
    public static final String EXCEL_SHEET_FILE_PATH = "src//main//resources//bonus_cap_certificate.xlsm";

    public static SimulatorCalculator simulatorCalculator = new SimulatorCalculator();

    public static  PayoffCalculator payoffCalculator = new PayoffCalculator();

    public static MarketRiskMeasuresCalculator marketRiskMeasuresCalculator = new MarketRiskMeasuresCalculator();

    public static void main( String[] args )throws IOException, InvalidFormatException
    {
        HashMap<String, Object> instrumentDetails = new LinkedHashMap<>();
        HashMap<String, Object> historicalPrices = new LinkedHashMap<>();

        // Creating a Workbook from an Excel file
        Workbook workbook = WorkbookFactory.create(new File(EXCEL_SHEET_FILE_PATH));

        // Retrieving the number of sheets in the Workbook
        System.out.println("Workbook has " + workbook.getNumberOfSheets() + " Sheets : ");
        System.out.println("Retrieving Sheets");
        workbook.forEach(sheet -> {
            System.out.println("=> " + sheet.getSheetName());
        });

        // Getting the Sheet at index zero
        Sheet bonusCapCertificateSheet = workbook.getSheetAt(0);
        Sheet historicalPricesSheet = workbook.getSheetAt(1);

        // Create a DataFormatter to format and get each cell's value as String
        DataFormatter dataFormatter = new DataFormatter();

        List<String> attributes = getAttributes(bonusCapCertificateSheet, dataFormatter);
        List<Object> values = getValues(bonusCapCertificateSheet, dataFormatter);
        Map<String, Object> map = combineListsIntoOrderedMap(attributes,values);
        BonusCapCertificate bonusCapCertificate = null;
        try {
            bonusCapCertificate = createBonusCapCertificate(map);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        System.out.println(bonusCapCertificate.toString());
        long recommendedHoldingPeriod = calculateRecommendedHoldingPeriod(bonusCapCertificate);

        List<HistoricalPrices> historicalPricesList = new ArrayList<>();
        int totalNumberOfSimulations = bonusCapCertificate.getNumberOfSimulations();
        int maxSimulationsPerNode = 50;

        System.out.println("recommended holding period = [" + recommendedHoldingPeriod + "]");
        System.out.println("number of simulations = [" + totalNumberOfSimulations + "]");
        System.out.println("maximum simulations per node = [" + maxSimulationsPerNode + "]");

        System.out.println("\n\nHISTORICAL PRICES SHEET \n");
        historicalPricesList = getHistoricalPrices(historicalPricesSheet,dataFormatter);
        System.out.println(historicalPricesList.toString());

        try (Ignite ignite = Ignition.start("examples/config/example-ignite.xml")) {
            List<List<Double>> computedSimsPayoff = ignite.compute().call(jobs(ignite.cluster().nodes().size(), bonusCapCertificate,
                        historicalPricesList, recommendedHoldingPeriod),
                    new IgniteReducer<List<Double>,List<List<Double>>>() {
                        private List<List<Double>> reduced = new ArrayList<>();
                        @Override
                        public boolean collect(@Nullable List<Double> objects) {
                            reduced.add(objects);
                            return true;
                        }

                        @Override
                        public List<List<Double>> reduce() {
                            return reduced;
                        }
                    });

            BigDecimal[][] discountedPayoffs = payoffCalculator.calculatedDiscountedPayoffs(recommendedHoldingPeriod,bonusCapCertificate,nonDiscountedPayoffs);
            marketRiskMeasuresCalculator.calculateMarketRiskMeasures(discountedPayoffs,bonusCapCertificate,recommendedHoldingPeriod);
            // Closing the workbook
        }
        workbook.close();
    }

    private static Collection<IgniteCallable<List<Double>>> jobs(int clusterSize, final BonusCapCertificate bonusCapCertificate,
                                                                 final List<HistoricalPrices> historicalPrices, final long recommendedHoldingPeriod) {
        int nodes = clusterSize;
        int simulationChunks = bonusCapCertificate.getNumberOfSimulations() / nodes;
        bonusCapCertificate.setNumberOfSimulations(simulationChunks);

        Collection<IgniteCallable<List<Double>>> clos = new ArrayList<>(clusterSize);

        for (int i = 0; i < clusterSize; i++) {
            clos.add(new IgniteCallable<List<Double>>() {
                /** {@inheritDoc} */
                @Override
                public List<Double> call() throws Exception {
                    return simulatorCalculator.calculateSimulationsAndPayoffs(bonusCapCertificate, historicalPrices,
                            recommendedHoldingPeriod);
                }
            });
        }

        return clos;
    }

    private static long calculateRecommendedHoldingPeriod(BonusCapCertificate bonusCapCertificate) {
        return DAYS.between(bonusCapCertificate.getFinalValuationDate(),bonusCapCertificate.getSimulationStartDate()) * (-1);
    }

    private static Map<String,Object> combineListsIntoOrderedMap (List<String> keys, List<Object> values) {
        if (keys.size() != values.size())
            throw new IllegalArgumentException ("Cannot combine lists with dissimilar sizes");
        Map<String,Object> map = new LinkedHashMap<String,Object>();
        for (int i=0; i<keys.size(); i++) {
            map.put(keys.get(i), values.get(i));
        }
        return map;
    }

    private static BonusCapCertificate createBonusCapCertificate(Map<String,Object> map) throws ParseException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        BonusCapCertificate bonusCapCertificate = new BonusCapCertificate();
        bonusCapCertificate.setBarrier(Integer.parseInt((String) map.get(Constants.BARRIER)));
        bonusCapCertificate.setCap(Integer.parseInt((String) map.get(Constants.CAP)));
        bonusCapCertificate.setBonusAmount(new BigDecimal(((String) map.get(Constants.BONUS_AMOUNT))));
        bonusCapCertificate.setMultiplier(new BigDecimal((String)  map.get(Constants.MULTIPLIER)));
        bonusCapCertificate.setIssueDate(LocalDate.parse((String) map.get(Constants.ISSUE_DATE), formatter));
        bonusCapCertificate.setFinalValuationDate(LocalDate.parse((String) map.get(Constants.FINAL_VALUATION_DATE), formatter));
        bonusCapCertificate.setMaturityDate(LocalDate.parse((String) map.get(Constants.MATURITY_DATE), formatter));
        bonusCapCertificate.setPriipPrice(new BigDecimal((String) map.get(Constants.PRIIP_PRICE)));
        bonusCapCertificate.setStockPrice(new BigDecimal((String) map.get(Constants.STOCK_PRICE)));
        bonusCapCertificate.setSimulationStartDate(LocalDate.parse((String) map.get(Constants.SIMULATION_START_DATE), formatter));
        String rate = (String) map.get(Constants.RATE);
        String rateStr = rate.substring(0, rate.length() - 1);
        bonusCapCertificate.setRate(new BigDecimal(rateStr));
        bonusCapCertificate.setNumberOfSimulations(Integer.parseInt((String) map.get(Constants.NUMBER_OF_SIMULATIONS)));
        bonusCapCertificate.setqLabDemo((String) map.get(Constants.Q_LAB_DEMO));
        bonusCapCertificate.setMrmResultsSheetName((String) map.get(Constants.MRM_RESULTS_SHEET_NAME));
        String brownianToleranceLimit = (String) map.get(Constants.BROWNIAN_TOLERANCE_LIMIT);
        String brownianToleranceLimitStr = brownianToleranceLimit.substring(0, brownianToleranceLimit.length() - 1);
        bonusCapCertificate.setBrownianToleranceUnit(new BigDecimal(brownianToleranceLimitStr));
        bonusCapCertificate.setrScriptPath((String) map.get(Constants.RSCRIPT_PATH));
        bonusCapCertificate.setSummaryOutput((String) map.get(Constants.SUMMARY_OUTPUT));

        return bonusCapCertificate;
    }

    private static List<Object> getValues(Sheet bonusCapCertificateSheet, DataFormatter dataFormatter) {
        List<Object> valuesList = new ArrayList<>();
        System.out.println("\n\nBONUS CAP CERTIFICATE SHEET \n");
        Iterator<Row> rowIterator = bonusCapCertificateSheet.rowIterator();
        while(rowIterator.hasNext()){
            Row row = rowIterator.next();
            if(row.getRowNum() == 0){
                continue;
            }
            if(row.getRowNum() > 18){
                break;
            }
            Iterator<Cell> cellIterator = row.cellIterator();
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                int columnIndex = cell.getColumnIndex();
                if(columnIndex == 1) {
                    if(cell.getCellTypeEnum().equals(CellType.NUMERIC) && DateUtil.isCellDateFormatted(cell)){
                        valuesList.add(new SimpleDateFormat("dd/MM/yyyy").format(cell.getDateCellValue()));
                    }else{
                        valuesList.add(dataFormatter.formatCellValue(cell));
                    }
                }
            }
        }
        return valuesList;
    }

    private static List<String> getAttributes(Sheet bonusCapCertificateSheet, DataFormatter dataFormatter) {
        List<String> attributesList = new ArrayList<>();
        System.out.println("\n\nBONUS CAP CERTIFICATE SHEET \n");
        Iterator<Row> rowIterator = bonusCapCertificateSheet.rowIterator();
        while(rowIterator.hasNext()){
            Row row = rowIterator.next();
            if(row.getRowNum() == 0){
                continue;
            }
            if(row.getRowNum() > 18){
                break;
            }
            Iterator<Cell> cellIterator = row.cellIterator();
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                int columnIndex = cell.getColumnIndex();
                if(columnIndex == 0) {
                    String cellValue = dataFormatter.formatCellValue(cell);
                    attributesList.add(cellValue);
                    System.out.print(cellValue + "\t");
                }
            }
            System.out.println();
        }
        return attributesList;
    }

    private static List<HistoricalPrices> getHistoricalPrices(Sheet historicalPricesSheet, DataFormatter dataFormatter) {
        List<HistoricalPrices> historicalPricesList = new ArrayList<>();
        System.out.println("\n\nHISTORICAL PRICES SHEET \n");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        Iterator<Row> rowIterator = historicalPricesSheet.rowIterator();
        HistoricalPrices historicalPrices = null;
        while(rowIterator.hasNext()){
            Row row = rowIterator.next();
            if(row.getRowNum() == 0){
                continue;
            }
            Iterator<Cell> cellIterator = row.cellIterator();
            //date
            Cell dateCell = cellIterator.next();
            //close
            Cell closingPriceCell = cellIterator.next();
            historicalPrices = new HistoricalPrices();
            historicalPrices.setDate(LocalDate.parse((String)(new SimpleDateFormat("dd/MM/yyyy").format(dateCell.getDateCellValue())), formatter));
            historicalPrices.setClosingPrice(new BigDecimal(dataFormatter.formatCellValue(closingPriceCell)));
            historicalPricesList.add(historicalPrices);
            }
        return historicalPricesList;
    }
}
