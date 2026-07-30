package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoubleDamageToOpponentsAndTheirPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.PreventHalfDamageToControllerAndTheirPermanentsEffect;

@CardRegistration(set = "AVR", collectorNumber = "209")
public class GiselaBladeOfGoldnight extends Card {

    public GiselaBladeOfGoldnight() {
        addEffect(EffectSlot.STATIC, new DoubleDamageToOpponentsAndTheirPermanentsEffect());
        addEffect(EffectSlot.STATIC, new PreventHalfDamageToControllerAndTheirPermanentsEffect());
    }
}
