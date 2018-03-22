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

    public List<Double> calculateSimulationsAndPayoffs(BonusCapCertificate bonusCapCertificate,
                                               List<HistoricalPrices> historicalPricesList,
                                               long recommendedHoldingPeriod) {
        BigDecimal[][] simulations = createSimulations(bonusCapCertificate, historicalPricesList, recommendedHoldingPeriod);
        List<Double> nonDiscountedPayoffs = payoffCalculator.calculateNonDiscountedPayoffs(simulations, recommendedHoldingPeriod, bonusCapCertificate);
        return nonDiscountedPayoffs;
    }

    public BigDecimal[][] createSimulations(BonusCapCertificate bonusCapCertificate,
                                                  List<HistoricalPrices> historicalPricesList,
                                                  long recommendedHoldingPeriod) {
        BigDecimal riskFreeRate = (bonusCapCertificate.getRiskFreeRate()!=null)? bonusCapCertificate.getRiskFreeRate() : new BigDecimal(0);
        BigDecimal[][] adjustedReturns = createAdjustedReturns(historicalPricesList,riskFreeRate);
        BigDecimal[][] samples = getSamples(adjustedReturns, recommendedHoldingPeriod, bonusCapCertificate.getNumberOfSimulations());
        BigDecimal simulationStartLevel = getSimulationStartLevel(bonusCapCertificate.getSimulationStartDate(), historicalPricesList);
        BigDecimal[][] simulatedValues = performSimulations(samples,simulationStartLevel,recommendedHoldingPeriod, bonusCapCertificate.getNumberOfSimulations());
        return simulatedValues;
    }

    public BigDecimal[][] performSimulations(BigDecimal[][] samples, BigDecimal initialReferenceLevel,long recommendedHoldingPeriod, int numberOfSimulations) {
        int rhp = (int) recommendedHoldingPeriod;
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
                double exponent = Math.exp(samples[i][j].doubleValue());
                double refLevels = referenceLevels[i][j].doubleValue();
                samples[i][j] = BigDecimal.valueOf(exponent * refLevels);
            }
        }

        return cumulativeProduct(samples, rhp, numberOfSimulations);
    }

    public BigDecimal[][] cumulativeProduct(BigDecimal[][] samples, int rhp, int numberOfSimulations){
        BigDecimal[][] cumProd = new BigDecimal[rhp][numberOfSimulations];
        for(int i =0; i< rhp ; i++){
            for(int j =0; j< numberOfSimulations; j++){
                if(i==0) {
                    cumProd[i][j] = samples[i][j];
                }else{
                    cumProd[i][j] = BigDecimal.valueOf(samples[i][j].doubleValue() * samples[0][j].doubleValue());
                }
            }
        }
        return cumProd;
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
                if(adjustedReturns!= null) {
                    samples[i][j] = getRandom(adjustedReturns);
                }
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

        BigDecimal[] actualLogs = new BigDecimal[historicalPricesList.size()];
        BigDecimal[] logReturns = new BigDecimal[historicalPricesList.size()];
        BigDecimal[][] adjustedReturns = new BigDecimal[1][historicalPricesList.size()];
        for(int i=0; i< historicalPricesList.size(); i++){
            actualLogs[i] = BigDecimal.valueOf(Math.log(historicalPricesList.get(i).getClosingPrice().doubleValue()));
            //System.out.println(" actualLogs["+i+"] " + actualLogs[i]);
        }

        for(int i=0; i< actualLogs.length-1; i++){
            logReturns[i] = BigDecimal.valueOf(actualLogs[i+1].doubleValue() - actualLogs[i].doubleValue());
            //System.out.println(" logReturns["+i+"] " + logReturns[i]);
        }

        double[] logReturnsInput = new double[historicalPricesList.size()];

        for(int i=0; i< logReturns.length-1; i++){
            logReturnsInput[i] = logReturns[i].doubleValue();
            //System.out.println(" logReturnsInput["+i+"] " + logReturnsInput[i]);
        }

        StandardDeviation sd = new StandardDeviation(false);
        double standardDeviation = sd.evaluate(logReturnsInput);
        Mean mean = new Mean();
        double meanResult = mean.evaluate(logReturnsInput);
        int columns = logReturns.length-1;
        int rows = 1;

        for (int c = 0; c < columns; c++) {
            for (int r = 0; r < rows; r++) {
                adjustedReturns[r][c] = BigDecimal.valueOf(logReturns[c].doubleValue()
                        + (riskFreeRate.doubleValue() - meanResult - 0.5 * standardDeviation * standardDeviation));
                //System.out.println(" adjustedReturns["+r+"]["+c+"]"  + adjustedReturns[r][c]);
            }
        }

        return adjustedReturns;
    }
}

