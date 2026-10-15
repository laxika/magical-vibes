package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;

@CardRegistration(set = "INV", collectorNumber = "183")
public class BlurredMongoose extends Card {

    public BlurredMongoose() {
        addEffect(EffectSlot.STATIC, new CantBeCounteredEffect());
    }
}
