package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoubleDamageToControllerAndSelfEffect;

@CardRegistration(set = "SOI", collectorNumber = "162")
public class GoldnightCastigator extends Card {

    public GoldnightCastigator() {
        addEffect(EffectSlot.STATIC, new DoubleDamageToControllerAndSelfEffect());
    }
}
