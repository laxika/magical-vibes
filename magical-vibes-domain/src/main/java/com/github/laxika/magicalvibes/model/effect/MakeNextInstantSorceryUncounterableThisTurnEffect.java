package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import java.util.List;

/**
 * One-shot effect: the next instant or sorcery spell its controller casts this turn can't be
 * countered.
 */
public record MakeNextInstantSorceryUncounterableThisTurnEffect() implements NextMatchingSpellEffect {

    @Override
    public CardPredicate predicate() {
        return new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT), new CardTypePredicate(CardType.SORCERY)));
    }
}
