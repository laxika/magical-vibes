package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

/**
 * Registers a one-shot effect that gives the next spell its controller casts this turn convoke.
 */
public record GrantConvokeToNextSpellThisTurnEffect() implements NextMatchingSpellEffect {

    @Override
    public CardPredicate predicate() {
        return new CardTruePredicate();
    }
}
