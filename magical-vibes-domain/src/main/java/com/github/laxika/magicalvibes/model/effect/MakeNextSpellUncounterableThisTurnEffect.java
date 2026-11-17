package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

/**
 * Registers a one-shot effect that makes the next spell its controller casts this turn unable to
 * be countered.
 */
public record MakeNextSpellUncounterableThisTurnEffect() implements NextMatchingSpellEffect {

    @Override
    public CardPredicate predicate() {
        return new CardTruePredicate();
    }
}
