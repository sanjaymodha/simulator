package com.riskcare.simulator;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

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
        //double[] simulationsInput = new double[rhp*numberOfSimulations];
        List<Double> simulationsInput = new ArrayList<>();
        //int length = rhp*numberOfSimulations;
        for(int i =0; i< rhp ; i++){
            for(int j =0; j< numberOfSimulations; j++){
                //simulationsInput[i] = simulations[i][j].doubleValue();
                simulationsInput.add(simulations[i][j].doubleValue());
            }
        }

        Double[] toArray =  simulationsInput.toArray(new Double[simulationsInput.size()]);
        double[] input = Stream.of(toArray).mapToDouble(Double::doubleValue).toArray();

        StandardDeviation sd = new StandardDeviation(false);
        double standardDeviation = sd.evaluate(input);
        return nonDiscountedPayoffs;
    }

    public BigDecimal[][] calculatedDiscountedPayoffs(long recommendedHoldingPeriod, BonusCapCertificate bonusCapCertificate, List<Double> nonDiscountedPayoffs) {
        return new BigDecimal[0][];
    }
}
