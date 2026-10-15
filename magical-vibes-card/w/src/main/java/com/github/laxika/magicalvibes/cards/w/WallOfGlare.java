package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CanBlockAnyNumberOfCreaturesEffect;

@CardRegistration(set = "UDS", collectorNumber = "25")
public class WallOfGlare extends Card {

    public WallOfGlare() {
        addEffect(EffectSlot.STATIC, new CanBlockAnyNumberOfCreaturesEffect());
    }
}
