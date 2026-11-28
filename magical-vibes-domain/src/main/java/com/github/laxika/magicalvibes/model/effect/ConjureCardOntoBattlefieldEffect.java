package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect.SpellbookCard;

import java.util.Objects;
import java.util.Set;

/**
 * Puts a fresh card of {@code printing} onto the battlefield under the ability's controller. {@code conjure} is false
 * when the card was drafted from a spellbook rather than conjured, so "whenever you conjure"
 * abilities don't trigger; a conjured card is marked conjured. When {@code entryCounterType} is set, the permanent enters with
 * {@code entryCounterCount} counters of that type.
 */
public record ConjureCardOntoBattlefieldEffect(
        SpellbookCard printing,
        Set<CardType> enterTappedTypes,
        boolean conjure,
        CounterType entryCounterType,
        int entryCounterCount
) implements CardEffect {

    public ConjureCardOntoBattlefieldEffect(SpellbookCard printing,
                                            Set<CardType> enterTappedTypes, boolean conjure) {
        this(printing, enterTappedTypes, conjure, null, 0);
    }

    public ConjureCardOntoBattlefieldEffect {
        Objects.requireNonNull(printing, "printing");
        enterTappedTypes = Set.copyOf(Objects.requireNonNull(enterTappedTypes,
                "Enter-tapped types cannot be null"));
    }
}
