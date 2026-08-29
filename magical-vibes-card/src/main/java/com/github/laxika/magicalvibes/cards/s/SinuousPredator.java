package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedByAtMostNCreaturesEffect;

public class SinuousPredator extends Card {

    public SinuousPredator() {
        addEffect(EffectSlot.STATIC, new CanBeBlockedByAtMostNCreaturesEffect(1));
    }
}
