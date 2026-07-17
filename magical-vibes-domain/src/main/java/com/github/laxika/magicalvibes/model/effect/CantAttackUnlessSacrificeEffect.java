package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static attacking additional cost: this creature can't attack unless its controller sacrifices
 * {@code count} permanents matching {@code filter} (paid as attackers are declared). Leviathan —
 * "This creature can't attack unless you sacrifice two Islands."
 *
 * <p>The "can't attack unless you control enough to pay" legality gate is expressed separately with
 * a {@link CantAttackUnlessEffect} carrying a {@code ControlsPermanentCount} condition; this marker
 * only drives the actual sacrifice payment, applied by {@code AttackSacrificeCostService} as the
 * attack is committed. Read directly (no dispatch handler), mirroring
 * {@link EnchantedCreatureCantAttackUnlessPaysEffect}.
 *
 * @param count       number of permanents that must be sacrificed to attack
 * @param filter      which permanents the controller must sacrifice
 * @param description human-readable description of the cost (e.g. "two Islands")
 */
public record CantAttackUnlessSacrificeEffect(
        int count,
        PermanentPredicate filter,
        String description
) implements CardEffect {
}
