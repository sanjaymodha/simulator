package com.riskcare.simulator;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

/**
 * Created by smodha on 14/03/2018.
 */
public class SimulatorCalculator {
    private BonusCapCertificate bonusCapCertificate;
    private List<HistoricalPrices> historicalPricesList;
    private long recommendedHoldingPeriod;
    private PayoffCalculator payoffCalculator = new PayoffCalculator();
    private MarketRiskMeasuresCalculator marketRiskMeasuresCalculator = new MarketRiskMeasuresCalculator();

    public BonusCapCertificate getBonusCapCertificate() {
        return bonusCapCertificate;
    }

    public void setBonusCapCertificate(BonusCapCertificate bonusCapCertificate) {
        this.bonusCapCertificate = bonusCapCertificate;
    }

    public List<HistoricalPrices> getHistoricalPricesList() {
        return historicalPricesList;
    }

    public void setHistoricalPricesList(List<HistoricalPrices> historicalPricesList) {
        this.historicalPricesList = historicalPricesList;
    }

    public long getRecommendedHoldingPeriod() {
        return recommendedHoldingPeriod;
    }

    public void setRecommendedHoldingPeriod(long recommendedHoldingPeriod) {
        this.recommendedHoldingPeriod = recommendedHoldingPeriod;
    }

    public void calculateSimulationsAndPayoffs(BonusCapCertificate bonusCapCertificate,
                                               List<HistoricalPrices> historicalPricesList,
                                               long recommendedHoldingPeriod) {
        BigDecimal[] simulations = createSimulations(bonusCapCertificate, historicalPricesList, recommendedHoldingPeriod);
        BigDecimal[][] nonDiscountedPayoffs = payoffCalculator.calculateNonDiscountedPayoffs(simulations, bonusCapCertificate);
        BigDecimal[][] discountedPayoffs = payoffCalculator.calculatedDiscountedPayoffs(recommendedHoldingPeriod,bonusCapCertificate,nonDiscountedPayoffs);
        marketRiskMeasuresCalculator.calculateMarketRiskMeasures(discountedPayoffs,bonusCapCertificate,recommendedHoldingPeriod);

    }

    public BigDecimal[] createSimulations(BonusCapCertificate bonusCapCertificate,
                                                  List<HistoricalPrices> historicalPricesList,
                                                  long recommendedHoldingPeriod) {
        BigDecimal riskFreeRate = (bonusCapCertificate.getRiskFreeRate()!=null)? bonusCapCertificate.getRiskFreeRate() : new BigDecimal(0);
        BigDecimal[][] adjustedReturns = createAdjustedReturns(historicalPricesList,riskFreeRate);
        BigDecimal[][] samples = getSamples(adjustedReturns, recommendedHoldingPeriod, bonusCapCertificate.getNumberOfSimulations());
        BigDecimal simulationStartLevel = getSimulationStartLevel(bonusCapCertificate.getSimulationStartDate(), historicalPricesList);
        BigDecimal[][] simulatedValues = performSimulations(samples,simulationStartLevel,recommendedHoldingPeriod, bonusCapCertificate.getNumberOfSimulations());
        return null;
    }

    public BigDecimal[][] performSimulations(BigDecimal[][] samples, BigDecimal initialReferenceLevel,long recommendedHoldingPeriod, int numberOfSimulations) {
        int rhp = (int) recommendedHoldingPeriod;
        BigDecimal[][] simulatedValues = new BigDecimal[rhp][numberOfSimulations];
        BigDecimal[][] referenceLevels = new BigDecimal[rhp][numberOfSimulations];
        double ones = 1.0;

        for(int i =0; i< rhp ; i++){
            for(int j =0; j< numberOfSimulations; j++){
                if(i==0) {
                    referenceLevels[i][j] = initialReferenceLevel;
                }else{
                    referenceLevels[i][j] = BigDecimal.valueOf(ones);
                }
            }
        }

        for(int i =0; i< rhp ; i++){
            for(int j =0; j< numberOfSimulations; j++){
                samples[i][j] = BigDecimal.valueOf(Math.exp(samples[i][j].doubleValue()));
            }
        }
        System.out.println();
        /*for(int i =0; i< rhp ; i++){
            for(int j =0; j< numberOfSimulations; j++){
                samples[i][j] = BigDecimal.valueOf(Math.exp(samples[i][j].doubleValue() * referenceLevels[i][j].doubleValue()));
            }
        }*/

        return simulatedValues;
    }

    public BigDecimal getSimulationStartLevel(LocalDate simulationStartDate, List<HistoricalPrices> historicalPricesList) {
        BigDecimal result = null;
        LocalDate indexDate = simulationStartDate.minusDays(1);
        for(HistoricalPrices price: historicalPricesList){
            if(price.getDate().equals(indexDate)){
                result = price.getClosingPrice();
            }
        }
        return result;
    }

    public BigDecimal[][] getSamples(BigDecimal[][] adjustedReturns, long recommendedHoldingPeriod, int numberOfSimulations) {
        int rhp = (int) recommendedHoldingPeriod;
        BigDecimal[][] samples = new BigDecimal[rhp][numberOfSimulations];
        for(int i =0; i< rhp ; i++){
            for(int j =0; j< numberOfSimulations; j++){
                samples[i][j] = getRandom(adjustedReturns);
            }
        }
        return samples;
    }

    public BigDecimal getRandom(BigDecimal[][] adjustedReturns) {
        Random random = new Random();
        int rnd = random.nextInt(adjustedReturns[0].length -1);
        return adjustedReturns[0][rnd];
    }

    public BigDecimal[][] createAdjustedReturns(List<HistoricalPrices> historicalPricesList, BigDecimal riskFreeRate) {
        //List<BigDecimal> actualLogs = new ArrayList<>();

        BigDecimal[] actualLogs = new BigDecimal[historicalPricesList.size()];
        BigDecimal[] logReturns = new BigDecimal[historicalPricesList.size()];
        BigDecimal[][] adjustedReturns = new BigDecimal[1][historicalPricesList.size()];

        //double[] adjustedReturns = new double[1827];

        //List<BigDecimal> logReturns = new ArrayList<>();
        for(int i=0; i< historicalPricesList.size(); i++){
            actualLogs[i] = BigDecimal.valueOf(Math.log(historicalPricesList.get(i).getClosingPrice().doubleValue()));
        }

        /*for(HistoricalPrices price: historicalPricesList){
            actualLogs.add(BigDecimal.valueOf(Math.log(price.getClosingPrice().doubleValue())));
        }*/
        for(int i=0; i< actualLogs.length-1; i++){
            logReturns[i+1] = BigDecimal.valueOf(actualLogs[i+1].doubleValue() - actualLogs[i].doubleValue());
        }

        double[] logReturnsInput = new double[historicalPricesList.size()];

        for(int i=0; i< logReturns.length-1; i++){
            logReturnsInput[i+1] = logReturns[i+1].doubleValue();
        }

        StandardDeviation sd = new StandardDeviation(false);
        double standardDeviation = sd.evaluate(logReturnsInput);
        Mean mean = new Mean();
        double meanResult = mean.evaluate(logReturnsInput);
        int columns = logReturns.length-1;
        int rows = 1;

        for (int c = 0; c < columns; c++) {
            for (int r = 0; r < rows; r++) {
                adjustedReturns[r][c+1] = BigDecimal.valueOf(logReturns[c+1].doubleValue()
                        + (riskFreeRate.doubleValue() - meanResult - 0.5 * standardDeviation * standardDeviation));
            }
        }

        return adjustedReturns;
    }
}

