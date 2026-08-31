package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Target player gains {@code amount} life. The amount is a {@link DynamicAmount} evaluated at
 * resolution (fixed number, X paid, …) — e.g. Stream of Life's "target player gains X life".
 * A nonnegative {@code targetGroup} routes the effect to that activated-ability target group.
 */
public record TargetPlayerGainsLifeEffect(DynamicAmount amount, int targetGroup) implements CardEffect {

    public TargetPlayerGainsLifeEffect(DynamicAmount amount) {
        this(amount, -1);
    }

    public TargetPlayerGainsLifeEffect(int amount) {
        this(new Fixed(amount));
    }

    public TargetPlayerGainsLifeEffect(int amount, int targetGroup) {
        this(new Fixed(amount), targetGroup);
    }

    public static TargetPlayerGainsLifeEffect forTargetGroup(int amount, int targetGroup) {
        return new TargetPlayerGainsLifeEffect(amount, targetGroup);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
