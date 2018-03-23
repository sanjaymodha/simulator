package com.riskcare.simulator;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by smodha on 14/03/2018.
 */
public class PayoffCalculator {
    public List<Double> calculateNonDiscountedPayoffs(BigDecimal[][] simulations, long recommendedHoldingPeriod, BonusCapCertificate bonusCapCertificate) {
        List<Double> nonDiscountedPayoffs = new ArrayList<>();
        int barrier = bonusCapCertificate.getBarrier();
        int rhp = (int) recommendedHoldingPeriod;
        int numberOfSimulations = bonusCapCertificate.getNumberOfSimulations();
        BigDecimal brownianToleranceLimit = bonusCapCertificate.getBrownianToleranceUnit();
        List<Double> simulationsInput = new ArrayList<>();

        for(int i =0; i< rhp ; i++){
            for(int j =0; j< numberOfSimulations; j++){
                simulationsInput.add(simulations[i][j].doubleValue());
            }
        }

        Double[] toArray =  simulationsInput.toArray(new Double[simulationsInput.size()]);
        double[] input = Stream.of(toArray).mapToDouble(Double::doubleValue).toArray();

        StandardDeviation sd = new StandardDeviation(false);
        double standardDeviation = sd.evaluate(input);
        boolean isCapBarrier = false;

        Boolean[][] barrierEvents = getBarrierEvents(simulations, barrier, isCapBarrier,
                brownianToleranceLimit, standardDeviation, recommendedHoldingPeriod, numberOfSimulations);

        BigDecimal[] finalReferenceLevel = new BigDecimal[numberOfSimulations];

        for(int i=rhp-1; i<rhp ; i++){
            for(int j =0; j< numberOfSimulations; j++){
                finalReferenceLevel[j] = simulations[i][j];
            }
        }

        nonDiscountedPayoffs = doCalculatePayOffs(barrierEvents, finalReferenceLevel, bonusCapCertificate);

        System.out.println("calculating payoffs");

        return nonDiscountedPayoffs;
    }

    private List<Double> doCalculatePayOffs(Boolean[][] barrierEvents, BigDecimal[] finalReferenceLevel, BonusCapCertificate bonusCapCertificate) {
        List<Double> paymentsAtMaturity = new ArrayList<>();
        int cap = bonusCapCertificate.getCap();
        BigDecimal multiplier = bonusCapCertificate.getMultiplier();
        int numberOfSimulations = (int) bonusCapCertificate.getNumberOfSimulations();

        BigDecimal[][] payoffWithEvent = calculatePayoffWithEvent(finalReferenceLevel, cap, multiplier, numberOfSimulations);
        BigDecimal[][] payoffNoEvent = calculatePayoffNoEvent(finalReferenceLevel, cap, multiplier, numberOfSimulations);
        for(int i=0; i<1 ; i++){
            for(int j =0; j< numberOfSimulations; j++){
                paymentsAtMaturity.add(payoffNoEvent[i][j].doubleValue());
            }
        }

        return paymentsAtMaturity;
    }

    private BigDecimal[][] calculatePayoffNoEvent(BigDecimal[] finalReferenceLevel, int cap, BigDecimal multiplier, int numberOfSimulations) {
        BigDecimal[][] payoffNoEvent = new BigDecimal[1][numberOfSimulations];
        BigDecimal[][] maxFrlMultiplier = calculateMaxFinalReferenceLevelMultiplier(finalReferenceLevel, multiplier, numberOfSimulations);

        for(int i=0; i<1 ; i++){
            for(int j =0; j< numberOfSimulations; j++){
                payoffNoEvent[0][j] = (maxFrlMultiplier[i][j].doubleValue() > (double) cap) ? BigDecimal.valueOf((double) cap): maxFrlMultiplier[i][j];
            }
        }
        return payoffNoEvent;
    }

    @NotNull
    private BigDecimal[][] calculateMaxFinalReferenceLevelMultiplier(BigDecimal[] finalReferenceLevel, BigDecimal multiplier, int numberOfSimulations) {
        BigDecimal[][] maxFrlMultiplier = new BigDecimal[1][numberOfSimulations];
        for(int i=0; i<1 ; i++){
            for(int j =0; j< numberOfSimulations; j++){
                double frlMultiplier = finalReferenceLevel[j].doubleValue() * multiplier.doubleValue();
                maxFrlMultiplier[0][j] = (frlMultiplier > multiplier.doubleValue()) ? BigDecimal.valueOf(frlMultiplier): multiplier;
            }
        }
        return maxFrlMultiplier;
    }

    private BigDecimal[][] calculatePayoffWithEvent(BigDecimal[] finalReferenceLevel, int cap, BigDecimal multiplier, int numberOfSimulations) {
        BigDecimal[][] payoffWithEvent = new BigDecimal[1][numberOfSimulations];
        for(int i=0; i<1 ; i++){
            for(int j =0; j< numberOfSimulations; j++){
                double frlMultiplier = finalReferenceLevel[j].doubleValue() * multiplier.doubleValue();
                payoffWithEvent[0][j] = (frlMultiplier > (double) cap) ? BigDecimal.valueOf((double) cap ): BigDecimal.valueOf(frlMultiplier);
            }
        }
        return payoffWithEvent;
    }


    private Boolean[][] getBarrierEvents(BigDecimal[][] simulations, int barrier, boolean isCapBarrier,
                                        BigDecimal brownianToleranceLimit, double standardDeviation,
                                        long recommendedHoldingPeriod, int numberOfSimulations) {
        System.out.println("get barrier events");
        Boolean[][] theProbability = new Boolean[1][numberOfSimulations];
        BigDecimal[][] probabilities = probabilityOfBreach(simulations, barrier, isCapBarrier, standardDeviation, recommendedHoldingPeriod, numberOfSimulations);

        System.out.println("calculated probabilities");
        for(int i=0; i<1 ; i++){
            for(int j =0; j< numberOfSimulations; j++){
                theProbability[0][j] = ((probabilities[i][j].doubleValue() >= (1.0 - brownianToleranceLimit.doubleValue()))) ? true : false;
            }
        }

        System.out.println("end barrier events");

        return theProbability; // probability
    }

    private BigDecimal[][] probabilityOfBreach(BigDecimal[][] simulations, int barrier,
                                           boolean isCapBarrier, double standardDeviation,
                                           long recommendedHoldingPeriod, int numberOfSimulations) {
        BigDecimal[][] defaultResultProbabilities = new BigDecimal[1][numberOfSimulations];
        BigDecimal[][] startPrice = new BigDecimal[1][numberOfSimulations];
        BigDecimal[][] endPrice = new BigDecimal[1][numberOfSimulations];
        int rhp = (int) recommendedHoldingPeriod;
        BigDecimal[][] noBreachProbability = new BigDecimal[1][rhp];
        double ones = 1.0;
        // default result probabilities
        calculateDefaulResultProbabilities(numberOfSimulations, defaultResultProbabilities, ones);
        // start price
        calculateStartPrice(simulations, numberOfSimulations, startPrice);
        // end price
        calculateEndPrice(simulations, numberOfSimulations, endPrice, rhp);

        if(!isCapBarrier){
            BigDecimal[][] minPrice = new BigDecimal[1][numberOfSimulations];
            calculateMinimumPrice(numberOfSimulations, startPrice[0], endPrice[0], minPrice);

            Boolean[][] simulationProbabilities = new Boolean[1][numberOfSimulations];
            calculateSimulationProbabilities(simulations, barrier, numberOfSimulations, rhp, simulationProbabilities);

            Boolean[][] startEndPrice = new Boolean[1][numberOfSimulations];
            calculateStartEndPriceWithRespectToBarrier(barrier, numberOfSimulations, minPrice[0], startEndPrice);

            Boolean[][] noBreach = new Boolean[1][numberOfSimulations];
            calculateNoBreach(numberOfSimulations, simulationProbabilities[0], startEndPrice[0], noBreach);
        }

        return calculateProbabilityOfNoBreach(startPrice, endPrice, standardDeviation, barrier, numberOfSimulations, rhp);
    }

    private void calculateNoBreach(int numberOfSimulations, Boolean[] simulationProbability, Boolean[] booleen, Boolean[][] noBreach) {
        for(int i=0; i<1 ; i++){
            for(int j =0; j< numberOfSimulations; j++){
                noBreach[0][j] = (simulationProbability[j] ^ booleen[j]) ? true : false;
            }
        }
    }

    private void calculateStartEndPriceWithRespectToBarrier(double barrier, int numberOfSimulations, BigDecimal[] bigDecimals, Boolean[][] startEndPrice) {
        for(int i=0; i<1 ; i++){
            for(int j =0; j< numberOfSimulations; j++){
                startEndPrice[0][j] = (barrier < bigDecimals[j].doubleValue()) ? true : false;
            }
        }
    }

    private void calculateSimulationProbabilities(BigDecimal[][] simulations, double barrier, int numberOfSimulations, int rhp, Boolean[][] simulationProbabilities) {
        for(int i=0; i<1 ; i++){
            for(int j =0; j< numberOfSimulations; j++){
                simulationProbabilities[0][j] = ((simulations[i][j].doubleValue() <= barrier)) ? true : false;
            }
        }
    }

    private void calculateMinimumPrice(int numberOfSimulations, BigDecimal[] bigDecimals, BigDecimal[] bigDecimals1, BigDecimal[][] minPrice) {
        for(int i=0; i<1 ; i++){
            for(int j =0; j< numberOfSimulations; j++){
                minPrice[0][j] = (bigDecimals[j].doubleValue() > bigDecimals1[j].doubleValue()) ? bigDecimals1[j] : bigDecimals[j];
            }
        }
    }

    private void calculateEndPrice(BigDecimal[][] simulations, int numberOfSimulations, BigDecimal[][] endPrice, int rhp) {
        for(int i=rhp-1; i<rhp ; i++){
            for(int j =0; j< numberOfSimulations; j++){
                endPrice[0][j] = simulations[i][j];
            }
        }
    }

    private void calculateStartPrice(BigDecimal[][] simulations, int numberOfSimulations, BigDecimal[][] startPrice) {
        for(int i=0; i<1 ; i++){
            for(int j =0; j< numberOfSimulations; j++){
                startPrice[0][j] = simulations[i][j];
            }
        }
    }

    private void calculateDefaulResultProbabilities(int numberOfSimulations, BigDecimal[][] defaultResultProbabilities, double ones) {
        for(int i=0; i<1 ; i++){
            for(int j =0; j< numberOfSimulations; j++){
                defaultResultProbabilities[i][j] = BigDecimal.valueOf(ones);
            }
        }
    }

    private BigDecimal[][] calculateProbabilityOfNoBreach(BigDecimal[][] startPrice, BigDecimal[][] endPrice,
                                                          double standardDeviation, int barrier, int numberOfSimulations,
                                                          int noOfDays) {
        //BigDecimal[][] logOfBarrierByStartPrice = new BigDecimal[1][noOfDays];
        //BigDecimal[][] logOfBarrierByEndPrice = new BigDecimal[1][noOfDays];
        BigDecimal[][] startEndLn = new BigDecimal[1][numberOfSimulations];
        BigDecimal[][] probability = new BigDecimal[1][numberOfSimulations];
        for(int i=0; i<1 ; i++){
            for(int j =0; j< numberOfSimulations; j++){
                double logOfBarrierByStartPrice = Math.log((double)barrier / startPrice[i][j].doubleValue());
                double logOfBarrierByEndPrice = Math.log((double)barrier / endPrice[i][j].doubleValue());
                startEndLn[0][j] = BigDecimal.valueOf(logOfBarrierByStartPrice * logOfBarrierByEndPrice);
            }
        }

        double sd_t = standardDeviation * Math.sqrt(noOfDays/365);
        double vol = sd_t * sd_t;

        for(int i=0; i<1 ; i++){
            for(int j =0; j< numberOfSimulations; j++){
                probability[0][j] = BigDecimal.valueOf(1.0 - (Math.exp(-2 * startEndLn[i][j].doubleValue()/vol)));
            }
        }
        return probability;
    }


    public List<Double> calculatedDiscountedPayoffs(long recommendedHoldingPeriod, BonusCapCertificate bonusCapCertificate, List<Double> nonDiscountedPayoffs) {
        int rhp = (int) recommendedHoldingPeriod;
        BigDecimal rate = bonusCapCertificate.getRate();
        double discount = Math.exp(-1 * rhp/360 * rate.doubleValue());
        List<Double> pvOfPayments = new ArrayList<>();
        for(Double nondiscountedPayoff: nonDiscountedPayoffs){
            pvOfPayments.add(nondiscountedPayoff * discount);
        }
        return pvOfPayments;
    }
}
