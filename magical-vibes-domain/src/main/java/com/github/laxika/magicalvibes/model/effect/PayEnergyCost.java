package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.XValue;

/**
 * Pays energy counters (CR 107.14) as an activation cost, a forced cost, or a non-mana alternative spell cost. The
 * amount is a {@link Fixed} number or {@link XValue}, the X announced as the ability is activated ("Pay X {E}",
 * CR 107.3a); the activation paths evaluate it with the activation's X ({@code AmountEvaluationService.evaluateCost}).
 * The forced-cost and alternative-spell-cost paths take only a fixed amount ({@link #fixedAmount()}). A player can't
 * pay more energy than they have (CR 118.3).
 */
public record PayEnergyCost(DynamicAmount amount) implements AlternativeSpellCost {

    public PayEnergyCost {
        if (amount instanceof Fixed fixed && fixed.value() < 0) {
            throw new IllegalArgumentException("Energy cost must not be negative");
        }
    }

    public PayEnergyCost(int amount) {
        this(new Fixed(amount));
    }

    /** "Pay X {E}", X announced as the ability is activated. */
    public static PayEnergyCost x() {
        return new PayEnergyCost(new XValue());
    }

    @Override
    public Kind kind() {
        return Kind.PAY_ENERGY;
    }

    /**
     * The fixed energy payment, for the readers (forced costs, alternative spell costs) that only take a fixed amount.
     *
     * @throws IllegalStateException when the amount is not fixed
     */
    @Override
    public int fixedAmount() {
        if (amount instanceof Fixed fixed) {
            return fixed.value();
        }
        throw new IllegalStateException("The energy payment is " + amount + ", not fixed");
    }
}
