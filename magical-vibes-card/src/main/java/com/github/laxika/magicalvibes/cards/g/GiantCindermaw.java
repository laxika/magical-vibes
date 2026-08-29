package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PlayersCantGainLifeEffect;

@CardRegistration(set = "BRO", collectorNumber = "136")
public class GiantCindermaw extends Card {

    public GiantCindermaw() {
        addEffect(EffectSlot.STATIC, new PlayersCantGainLifeEffect());
    }
}
