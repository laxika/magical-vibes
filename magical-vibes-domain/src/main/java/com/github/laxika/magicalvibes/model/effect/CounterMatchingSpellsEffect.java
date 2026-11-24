package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * Non-targeting: counter every spell or ability on the stack whose kind is one of {@code types} and that matches
 * {@code filter}, judged for this effect's controller: overloaded Counterflux ("counter each spell you don't
 * control"), Swift Silence and Reverse the Polarity (every other spell), Kadena's Silencer ("all abilities your
 * opponents control", {@link #abilities}) and Glen Elendra's Answer ("all spells your opponents control and all
 * abilities your opponents control", {@link #spellsAndAbilities}). The resolving object is never countered by its
 * own effect. Each one is countered on its own through {@code CounterSupport.findCounterTarget}, so one that can't
 * be countered is skipped (CR 701.6a). The number countered is the entry's event value ("for each spell and ability
 * countered this way") and the COUNTER_SPELL step result.
 *
 * @param filter which of them, judged for the controller
 * @param types  the stack entry kinds it counters: the spell kinds by default
 */
public record CounterMatchingSpellsEffect(StackEntryPredicate filter, Set<StackEntryType> types)
        implements CounterSpellingEffect {

    /** Every spell kind of stack entry. */
    public static final Set<StackEntryType> SPELL_TYPES = Set.copyOf(EnumSet.complementOf(
            EnumSet.of(StackEntryType.ACTIVATED_ABILITY, StackEntryType.TRIGGERED_ABILITY)));

    public CounterMatchingSpellsEffect {
        Objects.requireNonNull(filter, "filter is required");
        types = Set.copyOf(types);
        if (types.isEmpty()) {
            throw new IllegalArgumentException("types must name at least one stack entry kind");
        }
    }

    /** "Counter each spell [matching filter]". */
    public CounterMatchingSpellsEffect(StackEntryPredicate filter) {
        this(filter, SPELL_TYPES);
    }

    /** "Counter all abilities [matching filter]": activated and triggered abilities. */
    public static CounterMatchingSpellsEffect abilities(StackEntryPredicate filter) {
        return new CounterMatchingSpellsEffect(filter, StackEntryType.ABILITIES);
    }

    /** "Counter all spells and all abilities [matching filter]". */
    public static CounterMatchingSpellsEffect spellsAndAbilities(StackEntryPredicate filter) {
        return new CounterMatchingSpellsEffect(filter, StackEntryType.SPELLS_AND_ABILITIES);
    }
}
