package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Static effect (a permanent's or an emblem's): the players {@code players} names can't cast spells matching the
 * supplied predicate: the opponents of its controller by default, or every player ({@link Subjects#eachPlayer()},
 * Archon of Valor's Reach).
 *
 * @param predicate which spells
 *
 * @param players who is restricted, judged as the restriction is checked
 */
public record OpponentsCantCastSpellsMatchingPredicateEffect(CardPredicate predicate, PlayerSubject players) implements CardEffect {

    public OpponentsCantCastSpellsMatchingPredicateEffect {
        if (!(players instanceof PlayerSubject.Each)) {
            throw new IllegalArgumentException("Unsupported players for a static restriction: " + players);
        }
    }

    public OpponentsCantCastSpellsMatchingPredicateEffect(CardPredicate predicate) {
        this(predicate, Subjects.eachOpponent());
    }
}
