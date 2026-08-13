package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "BNG", collectorNumber = "65")
public class DrownInSorrow extends Card {

    public DrownInSorrow() {
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(-2, -2));
        addEffect(EffectSlot.SPELL, new ScryEffect(1));
    }
}
