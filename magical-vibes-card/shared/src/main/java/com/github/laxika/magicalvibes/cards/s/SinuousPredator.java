package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedByAtMostNCreaturesEffect;

public class SinuousPredator extends Card {

    public SinuousPredator() {
        addStaticEffect(new CanBeBlockedByAtMostNCreaturesEffect(1));
    }
}
