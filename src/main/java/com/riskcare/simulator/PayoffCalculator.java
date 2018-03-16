package com.riskcare.simulator;

import java.math.BigDecimal;

/**
 * Created by smodha on 14/03/2018.
 */
public class PayoffCalculator {
    public BigDecimal[][] calculateNonDiscountedPayoffs(BigDecimal[] simulations, BonusCapCertificate bonusCapCertificate) {
        return new BigDecimal[0][];
    }

    public BigDecimal[][] calculatedDiscountedPayoffs(long recommendedHoldingPeriod, BonusCapCertificate bonusCapCertificate, BigDecimal[][] nonDiscountedPayoffs) {
        return new BigDecimal[0][];
    }
}
