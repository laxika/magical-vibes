package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Instructs a permanent to endure the given amount: its controller chooses between putting that
 * many +1/+1 counters on it and creating an equal-sized white Spirit token.
 */
public record EndureEffect(DynamicAmount amount, Target target) implements CardEffect {

    public enum Target {
        SOURCE,
        TRIGGERING_PERMANENT
    }

    public EndureEffect(int amount) {
        this(new Fixed(validateFixedAmount(amount)), Target.SOURCE);
    }

    public EndureEffect(DynamicAmount amount) {
        this(amount, Target.SOURCE);
    }

    public static EndureEffect forTriggeringPermanent(DynamicAmount amount) {
        return new EndureEffect(amount, Target.TRIGGERING_PERMANENT);
    }

    public EndureEffect {
        if (amount == null) {
            throw new IllegalArgumentException("Endure amount cannot be null");
        }
        if (target == null) {
            throw new IllegalArgumentException("Endure target cannot be null");
        }
    }

    private static int validateFixedAmount(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Endure amount cannot be negative");
        }
        return amount;
    }
}
