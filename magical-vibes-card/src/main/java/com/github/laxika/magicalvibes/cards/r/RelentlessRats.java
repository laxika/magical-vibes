package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostByOtherCreaturesWithSameNameEffect;

@CardRegistration(set = "10E", collectorNumber = "173")
public class RelentlessRats extends Card {

    public RelentlessRats() {
        addEffect(EffectSlot.STATIC, new BoostByOtherCreaturesWithSameNameEffect(1, 1));
    }
}
