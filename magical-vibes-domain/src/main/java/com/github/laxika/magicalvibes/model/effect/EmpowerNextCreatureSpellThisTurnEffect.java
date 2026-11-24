package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

/**
 * One-shot effect: the next creature spell the resolving controller casts this turn can't be
 * countered (when {@code uncounterable}) and the creature enters with
 * {@code additionalPlusOneCounters} extra +1/+1 counters (Savage Summoning). A floating effect on the
 * player that the next creature spell cast ends. Pair with
 * {@link GrantFlashToNextMatchingSpellEffect} for the "as though it had flash" rider.
 *
 * @param uncounterable             whether the empowered creature spell can't be countered
 * @param additionalPlusOneCounters extra +1/+1 counters the creature enters with
 */
public record EmpowerNextCreatureSpellThisTurnEffect(boolean uncounterable,
                                                     int additionalPlusOneCounters) implements NextMatchingSpellEffect {

    @Override
    public CardPredicate predicate() {
        return new CardTypePredicate(CardType.CREATURE);
    }
}
