package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Deals damage to target opponent or planeswalker.
 * Can target a player (opponent only, not self) or a planeswalker permanent.
 * The amount is any {@link DynamicAmount} evaluated at resolution — a {@link Fixed}
 * constant (Burning Sun's Avatar) or a cost-snapshotted value such as an {@code XValue}
 * (Final Strike's sacrificed creature's power).
 *
 * @param amount the amount of damage to deal
 */
public record DealDamageToTargetOpponentOrPlaneswalkerEffect(DynamicAmount amount) implements CardEffect {

    public DealDamageToTargetOpponentOrPlaneswalkerEffect(int damage) {
        this(new Fixed(damage));
    }

    // The spec supplies the structural PLAYER_OR_PLANESWALKER + harmful checks; the kept
    // @ValidatesTarget validator (DamageTargetValidators) additionally enforces the opponent-only
    // relation for player targets, which the declarative spec cannot express.
    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.PLAYER_OR_PLANESWALKER);
    }
}
