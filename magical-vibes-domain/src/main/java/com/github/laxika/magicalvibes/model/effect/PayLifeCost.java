package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.HalfControllerLifeRoundedUp;
import com.github.laxika.magicalvibes.model.amount.Scaled;

/**
 * Cost effect: pay {@code amount} life, as an activation cost, an additional cost to cast a spell, a kicker or buyback
 * cost, or a forced cost. The amount is a {@link DynamicAmount} read against the payer as the total cost is determined
 * (CR 601.2f, 602.2b): a {@link Fixed} number, half the payer's life rounded up ({@link #halfLife()}, Murderous
 * Betrayal), a number per counter on the source ({@link #perSourceCounter}, Tornado), one per card in the payer's hand
 * (Hand of Vecna), or the colors in their commanders' color identity (War Room). Life can be paid only up to the life
 * total (CR 119.4, 118.3), and not while the payer's life total can't change (CR 119.8).
 *
 * <p>The engine evaluates the amount ({@code AmountEvaluationService.evaluateCost}) on the activation and forced-cost
 * paths. The spell paths (additional cast costs, kicker, buyback) take only a fixed amount ({@link #fixedAmount()}).
 */
public record PayLifeCost(DynamicAmount amount) implements CostEffect {

    public PayLifeCost(int amount) {
        this(new Fixed(amount));
    }

    /** "Pay half your life, rounded up." */
    public static PayLifeCost halfLife() {
        return new PayLifeCost(new HalfControllerLifeRoundedUp());
    }

    /** "Pay {@code amount} life for each {@code counterType} counter on this permanent." */
    public static PayLifeCost perSourceCounter(int amount, CounterType counterType) {
        return new PayLifeCost(new Scaled(new CountersOnSource(counterType), amount));
    }

    /**
     * The fixed life payment, for the readers (spell costs, kicker, buyback) that only take a fixed amount.
     *
     * @throws IllegalStateException when the amount is not fixed
     */
    public int fixedAmount() {
        if (amount instanceof Fixed fixed) {
            return fixed.value();
        }
        throw new IllegalStateException("The life payment is " + amount + ", not fixed");
    }
}
