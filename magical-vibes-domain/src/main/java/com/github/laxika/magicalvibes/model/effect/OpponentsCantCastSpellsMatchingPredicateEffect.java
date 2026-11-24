package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Static effect (a permanent's or an emblem's): the players {@code players} names can't cast spells matching the
 * supplied predicate: the opponents of its controller by default, every player ({@link Subjects#eachPlayer()},
 * Archon of Valor's Reach), or the enchanted permanent's controller ({@code controllerOf(attached())}, Brand of Ill
 * Omen).
 *
 * <p>The predicate sees the spell as it would be cast (its chosen X counted in its mana value, CR 202.3e); before X is
 * chosen, a spell with {X} is stopped only if every X would be. An amount in it ({@code CardManaValueAtMostAmountPredicate},
 * "greater than the number of lands that player controls" under a {@code CardNotPredicate}) is evaluated for the
 * casting player: "you" and "that player" of the amount are the player who would cast the spell.
 *
 * @param predicate which spells
 *
 * @param players who is restricted, judged as the restriction is checked
 */
public record OpponentsCantCastSpellsMatchingPredicateEffect(CardPredicate predicate, PlayerSubject players) implements CardEffect {

    public OpponentsCantCastSpellsMatchingPredicateEffect {
        if (!(players instanceof PlayerSubject.Each)
                && !(players instanceof PlayerSubject.ControllerOf(PermanentSubject.Attached(boolean creature, boolean last))
                && !creature && !last)) {
            throw new IllegalArgumentException("Unsupported players for a static restriction: " + players);
        }
    }

    public OpponentsCantCastSpellsMatchingPredicateEffect(CardPredicate predicate) {
        this(predicate, Subjects.eachOpponent());
    }
}
