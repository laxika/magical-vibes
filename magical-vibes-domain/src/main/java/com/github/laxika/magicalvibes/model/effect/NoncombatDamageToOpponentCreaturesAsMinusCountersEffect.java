package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect: if a source the controller controls would deal noncombat damage to a
 * creature an opponent controls, that many -1/-1 counters are put on that creature instead (CR 615).
 * Combat damage is unaffected. Used by Soul-Scar Mage.
 *
 * <p>Applies to any noncombat damage source the effect's controller controls, so — like wither —
 * the damage is still "dealt" (damage-triggered abilities, deathtouch, lifelink still see it); only
 * the marking is replaced by counters. Queried on the noncombat damage path via
 * {@code GameQueryService.noncombatDamageToOpponentCreatureAsCounters}; the combat path is separate,
 * so no combat check is needed.
 */
public record NoncombatDamageToOpponentCreaturesAsMinusCountersEffect() implements CardEffect {
}
