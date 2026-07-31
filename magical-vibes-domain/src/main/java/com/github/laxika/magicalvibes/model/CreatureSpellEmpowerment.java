package com.github.laxika.magicalvibes.model;

/**
 * A pending "the next creature spell you cast this turn" grant (Savage Summoning). Each unconsumed
 * empowerment is applied when its controller next casts a creature spell: {@code uncounterable}
 * marks that spell in {@code GameData.spellsMadeUncounterable}, and
 * {@code additionalPlusOneCounters} is added to {@code GameData.spellAdditionalEnterCounters} so the
 * creature enters with that many extra +1/+1 counters.
 *
 * @param uncounterable             whether the empowered spell can't be countered
 * @param additionalPlusOneCounters extra +1/+1 counters the resulting permanent enters with
 */
public record CreatureSpellEmpowerment(boolean uncounterable, int additionalPlusOneCounters) {
}
