package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Objects;

/**
 * "Exile the bottom [filter] card of your library": the matching card nearest the bottom of the controller's library
 * is exiled. The step records its {@code StepKind.EXILE}.
 */
public record ExileBottomMatchingCardOfLibraryEffect(CardPredicate filter) implements CardEffect {

    public ExileBottomMatchingCardOfLibraryEffect {
        Objects.requireNonNull(filter, "filter");
    }
}
