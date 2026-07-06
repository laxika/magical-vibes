package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Target player loses {@code amount} life. The amount is a {@link DynamicAmount} so relational
 * variants ("loses life equal to the life you gained", "loses 1 life for each Vampire you control")
 * are a single effect parameterized with the appropriate amount rather than one record per wording.
 */
public record TargetPlayerLosesLifeEffect(DynamicAmount amount) implements CardEffect {

    public TargetPlayerLosesLifeEffect(int amount) {
        this(new Fixed(amount));
    }

    @Override
    public boolean canTargetPlayer() {
        return true;
    }
}
