package com.riskcare.simulator;

import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by smodha on 14/03/2018.
 */
public class SimulatorCalulatorTests {
    private SimulatorCalculator simulatorCalculator = new SimulatorCalculator();

    @Test
    public void testAdjustedReturns(){
        List<HistoricalPrices> historicalPricesList = new ArrayList<>();

        HistoricalPrices historicalPrice1 = new HistoricalPrices();
        historicalPrice1.setDate(LocalDate.of(2012, Month.MARCH,15));
        historicalPrice1.setClosingPrice(new BigDecimal(19.91));

        HistoricalPrices historicalPrice2 = new HistoricalPrices();
        historicalPrice1.setDate(LocalDate.of(2012, Month.MARCH,16));
        historicalPrice1.setClosingPrice(new BigDecimal(20.03));

        historicalPricesList.add(historicalPrice1);
        historicalPricesList.add(historicalPrice2);

        //BigDecimal[] adjustedReturns = simulatorCalculator.createAdjustedReturns(historicalPricesList, new BigDecimal(0));
    }

}
