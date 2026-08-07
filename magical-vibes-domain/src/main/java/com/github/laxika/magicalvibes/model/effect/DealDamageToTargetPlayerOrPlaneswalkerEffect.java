package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;

/**
 * Deals damage to target player or planeswalker — both the "target player or planeswalker"
 * (Boggart Shenanigans) and "target opponent or planeswalker" (Burning Sun's Avatar) wordings.
 * The amount is any {@link DynamicAmount} evaluated at resolution — a {@link Fixed} constant or a
 * cost-snapshotted value such as an {@code XValue} (Brion Stoutarm's or Final Strike's sacrificed
 * creature's power).
 *
 * @param amount         the amount of damage to deal
 * @param playerRelation which players the effect may target. {@link PlayerRelation#ANY} is the
 *                       plain "target player" wording; {@link PlayerRelation#OPPONENT} narrows it
 *                       to "target opponent". The narrowing is not expressible in
 *                       {@link #targetSpec()} — the spec supplies the structural
 *                       player-or-planeswalker and harmful checks, and the {@code @ValidatesTarget}
 *                       escape hatch in {@code DamageTargetValidators} enforces the relation for
 *                       player targets. {@link PlayerRelation#SELF} is rejected: no printed card
 *                       makes its controller the only legal target of a targeted burn spell
 */
public record DealDamageToTargetPlayerOrPlaneswalkerEffect(DynamicAmount amount,
                                                           PlayerRelation playerRelation) implements CardEffect {

    public DealDamageToTargetPlayerOrPlaneswalkerEffect {
        if (playerRelation == PlayerRelation.SELF) {
            throw new IllegalArgumentException("SELF is not a targetable relation for damage to a player or planeswalker");
        }
    }

    public DealDamageToTargetPlayerOrPlaneswalkerEffect(DynamicAmount amount) {
        this(amount, PlayerRelation.ANY);
    }

    public DealDamageToTargetPlayerOrPlaneswalkerEffect(int damage) {
        this(new Fixed(damage), PlayerRelation.ANY);
    }

    public DealDamageToTargetPlayerOrPlaneswalkerEffect(int damage, PlayerRelation playerRelation) {
        this(new Fixed(damage), playerRelation);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.playerOrPlaneswalker());
    }

    @Override
    public PlayerRelation targetPlayerRelation() {
        return playerRelation;
    }
}
