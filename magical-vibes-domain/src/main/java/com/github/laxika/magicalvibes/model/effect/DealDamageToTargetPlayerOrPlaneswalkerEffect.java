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
 * @param unpreventable  {@code true} for the "the damage can't be prevented" wording (Flames of the
 *                       Blood Hand) — this one damage event ignores prevention, without the global
 *                       turn-wide lock of {@code DamageCantBePreventedThisTurnEffect}
 */
public record DealDamageToTargetPlayerOrPlaneswalkerEffect(DynamicAmount amount,
                                                           PlayerRelation playerRelation,
                                                           boolean unpreventable) implements CardEffect {

    public DealDamageToTargetPlayerOrPlaneswalkerEffect {
        if (playerRelation == PlayerRelation.SELF) {
            throw new IllegalArgumentException("SELF is not a targetable relation for damage to a player or planeswalker");
        }
    }

    public DealDamageToTargetPlayerOrPlaneswalkerEffect(DynamicAmount amount, PlayerRelation playerRelation) {
        this(amount, playerRelation, false);
    }

    public DealDamageToTargetPlayerOrPlaneswalkerEffect(DynamicAmount amount) {
        this(amount, PlayerRelation.ANY, false);
    }

    public DealDamageToTargetPlayerOrPlaneswalkerEffect(int damage) {
        this(new Fixed(damage), PlayerRelation.ANY, false);
    }

    public DealDamageToTargetPlayerOrPlaneswalkerEffect(int damage, boolean unpreventable) {
        this(new Fixed(damage), PlayerRelation.ANY, unpreventable);
    }

    public DealDamageToTargetPlayerOrPlaneswalkerEffect(int damage, PlayerRelation playerRelation) {
        this(new Fixed(damage), playerRelation, false);
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
