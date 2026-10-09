package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

public record GainActivatedAbilitiesOfExiledCardsEffect(boolean oncePerTurn, CardPredicate filter,
                                                       String abilityLink) implements CardEffect {

    public GainActivatedAbilitiesOfExiledCardsEffect(boolean oncePerTurn) {
        this(oncePerTurn, null, null);
    }

    public GainActivatedAbilitiesOfExiledCardsEffect() {
        this(false);
    }
}
