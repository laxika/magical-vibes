package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

/** Makes the source permanent a 0/0 copy of a creature card exiled with it and retains this effect's ability. */
public record BecomeCopyOfTargetExiledCreatureCardEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.exiledCards(new CardTypePredicate(CardType.CREATURE)));
    }
}
