package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Global static attacking additional cost: creatures matching {@code attackerPredicate} can't attack
 * unless their controller sacrifices {@code countPerAttacker} permanents matching
 * {@code sacrificeFilter} for each such attacking creature (paid as attackers are declared). Flooded
 * Woodlands — "Green creatures can't attack unless their controller sacrifices a land of their choice
 * for each green creature they control that's attacking."
 *
 * <p>Unlike {@link CantAttackUnlessSacrificeEffect} (a self-scoped marker on the restricted creature),
 * this applies from any permanent to every matching creature on any battlefield. The "can't declare
 * unless enough is controlled to pay" legality is derived from the same fields — checked per creature
 * in {@code AttackLegalityService} and for the whole declaration in
 * {@code CombatAttackService.declareAttackers} — and the payment is applied by
 * {@code AttackSacrificeCostService}. Read directly (no dispatch handler), mirroring
 * {@link EnchantedCreatureCantAttackUnlessPaysEffect}.
 *
 * @param attackerPredicate which attacking creatures the cost applies to
 * @param countPerAttacker  permanents that must be sacrificed per matching attacker
 * @param sacrificeFilter   which permanents the controller must sacrifice
 * @param description       human-readable description of the cost (e.g. "a land")
 */
public record CreaturesCantAttackUnlessSacrificeEffect(
        PermanentPredicate attackerPredicate,
        int countPerAttacker,
        PermanentPredicate sacrificeFilter,
        String description
) implements CardEffect {
}
