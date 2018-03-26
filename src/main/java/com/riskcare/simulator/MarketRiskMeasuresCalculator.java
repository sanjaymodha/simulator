package com.riskcare.simulator;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by smodha on 14/03/2018.
 */
public class MarketRiskMeasuresCalculator {
    public Map calculateMarketRiskMeasures(List<Double> discountedPayoffs, BonusCapCertificate bonusCapCertificate, long recommendedHoldingPeriod) {
        Map<String, Object> measures = new HashMap<>();
        double rhp = (double) recommendedHoldingPeriod;
        Double[] toArray =  discountedPayoffs.toArray(new Double[discountedPayoffs.size()]);
        double[] input = Stream.of(toArray).mapToDouble(Double::doubleValue).toArray();

        StandardDeviation sd = new StandardDeviation(false);
        double standardDeviation = sd.evaluate(input);
        Mean mean = new Mean();
        double meanResult = mean.evaluate(input);

        Double[] toDoubleArray =  discountedPayoffs.toArray(new Double[discountedPayoffs.size()]);
        double[] inputArray = Stream.of(toDoubleArray).mapToDouble(Double::doubleValue).toArray();

        DescriptiveStatistics desc = new DescriptiveStatistics(inputArray);
        double the_var = Math.abs(desc.getPercentile(0.025)); // percentile

        System.out.println("var: " + the_var);

        double Z = 1.96;
        // number of Trading Days
        double T = rhp / 365;

        System.out.println("T: "+T);

        double VeV = Math.sqrt(((Math.pow(Z,2) - 2) * (Math.log((the_var/bonusCapCertificate.getPriipPrice().doubleValue())))) - Z)/ Math.sqrt(T);

        if(Double.isNaN(VeV)){
            VeV = 0;
        }
        System.out.println("VeV: "+VeV);

        String marketRiskMeasure = getMarketRiskClass(VeV);

        System.out.println("marketRiskMeasure: "+marketRiskMeasure);
        measures.put("var", the_var);
        measures.put("mean", meanResult);
        measures.put("standardDeviation", standardDeviation);
        measures.put("VeV", VeV);
        measures.put("marketRiskMeasure", marketRiskMeasure);

        return measures;
    }

    private String getMarketRiskClass(double VeV) {
        String marketRiskClass = "N/A";
        if(VeV < 0.005){
            marketRiskClass = "1";
        }else if(VeV <= 0.005 && VeV < 0.05){
            marketRiskClass = "2";
        }else if(VeV <= 0.05 && VeV < 0.12){
            marketRiskClass = "3";
        }else if(VeV <= 0.12 && VeV < 0.20){
            marketRiskClass = "4";
        }else if(VeV <= 0.20 && VeV < 0.30){
            marketRiskClass = "5";
        }else if(VeV <= 0.30 && VeV < 0.80){
            marketRiskClass = "6";
        }else {
            marketRiskClass = "7";
        }

        return marketRiskClass;
    }
}
