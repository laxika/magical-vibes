package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.Objects;

/**
 * One-shot effect: the next spell matching {@code spellFilter} the resolving controller casts this turn can't be
 * countered (when {@code uncounterable}) and its permanent enters with {@code additionalPlusOneCounters} extra +1/+1
 * counters. A floating effect on the player that the next matching spell cast ends: the riders apply to that same
 * spell as it becomes cast (CR 601.2i), judged by the duration clock's own match, so a spell that ends the effect is
 * the one it applies to. "The next creature spell" (Insist, Savage Summoning) is the default filter; "the next instant
 * or sorcery spell" (Overmaster) and "the next spell" (Mistrise Village) use {@link #cantBeCountered}. Pair with
 * {@link GrantFlashToNextMatchingSpellEffect} for the "as though it had flash" rider.
 *
 * @param spellFilter               the spell the riders wait for
 * @param uncounterable             whether that spell can't be countered
 * @param additionalPlusOneCounters extra +1/+1 counters its permanent enters with
 */
public record EmpowerNextCreatureSpellThisTurnEffect(CardPredicate spellFilter, boolean uncounterable,
                                                     int additionalPlusOneCounters) implements NextMatchingSpellEffect {

    public EmpowerNextCreatureSpellThisTurnEffect {
        Objects.requireNonNull(spellFilter, "spellFilter");
    }

    /** "The next creature spell you cast this turn can't be countered [and enters with N additional counters]." */
    public EmpowerNextCreatureSpellThisTurnEffect(boolean uncounterable, int additionalPlusOneCounters) {
        this(new CardTypePredicate(CardType.CREATURE), uncounterable, additionalPlusOneCounters);
    }

    /** "The next [{@code spellFilter}] spell you cast this turn can't be countered." */
    public static EmpowerNextCreatureSpellThisTurnEffect cantBeCountered(CardPredicate spellFilter) {
        return new EmpowerNextCreatureSpellThisTurnEffect(spellFilter, true, 0);
    }

    @Override
    public CardPredicate predicate() {
        return spellFilter;
    }
}
