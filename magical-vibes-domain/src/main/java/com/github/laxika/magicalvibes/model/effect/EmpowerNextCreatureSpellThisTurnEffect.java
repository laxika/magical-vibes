package com.github.laxika.magicalvibes.model.effect;

/**
 * One-shot effect: the next creature spell the resolving controller casts this turn can't be
 * countered (when {@code uncounterable}) and the creature enters with
 * {@code additionalPlusOneCounters} extra +1/+1 counters (Savage Summoning). Tracked in
 * {@code GameData.nextCreatureSpellEmpowermentsThisTurn}, consumed by the next creature spell cast
 * and cleared at end of turn. Pair with
 * {@link GrantFlashToNextSpellOfTypeThisTurnEffect} for the "as though it had flash" rider.
 *
 * @param uncounterable             whether the empowered creature spell can't be countered
 * @param additionalPlusOneCounters extra +1/+1 counters the creature enters with
 */
public record EmpowerNextCreatureSpellThisTurnEffect(boolean uncounterable,
                                                     int additionalPlusOneCounters) implements CardEffect {
}
