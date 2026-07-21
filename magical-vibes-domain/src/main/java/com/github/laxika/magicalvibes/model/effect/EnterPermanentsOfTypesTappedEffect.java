package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;

/**
 * Replacement (STATIC): matching permanents enter tapped.
 * Match by {@code cardTypes} and/or an optional {@code filter} predicate.
 * When {@code opponentsOnly} is true, only permanents controlled by opponents of the source are affected.
 */
public record EnterPermanentsOfTypesTappedEffect(
        Set<CardType> cardTypes,
        boolean opponentsOnly,
        PermanentPredicate filter
) implements CardEffect {

    public EnterPermanentsOfTypesTappedEffect(Set<CardType> cardTypes) {
        this(cardTypes, false, null);
    }

    public EnterPermanentsOfTypesTappedEffect(Set<CardType> cardTypes, boolean opponentsOnly) {
        this(cardTypes, opponentsOnly, null);
    }

    /** Permanents matching {@code filter} enter tapped (optionally opponents-only). */
    public static EnterPermanentsOfTypesTappedEffect matching(PermanentPredicate filter, boolean opponentsOnly) {
        return new EnterPermanentsOfTypesTappedEffect(Set.of(), opponentsOnly, filter);
    }
}
