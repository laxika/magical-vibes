package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.OpponentExtraDrawsCreateTreasureEffect;

@CardRegistration(set = "MB2", collectorNumber = "124")
public class Hullbreacher extends Card {

    public Hullbreacher() {
        addEffect(EffectSlot.STATIC, new OpponentExtraDrawsCreateTreasureEffect());
    }
}
