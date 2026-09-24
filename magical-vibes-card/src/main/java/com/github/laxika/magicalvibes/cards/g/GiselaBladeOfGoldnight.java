package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoubleDamageToOpponentsAndTheirPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.PreventHalfDamageToControllerAndTheirPermanentsEffect;

@CardRegistration(set = "AVR", collectorNumber = "209")
@CardRegistration(set = "A25", collectorNumber = "204")
@CardRegistration(set = "CMM", collectorNumber = "338")
@CardRegistration(set = "CMM", collectorNumber = "579")
@CardRegistration(set = "CMM", collectorNumber = "682")
public class GiselaBladeOfGoldnight extends Card {

    public GiselaBladeOfGoldnight() {
        addEffect(EffectSlot.STATIC, new DoubleDamageToOpponentsAndTheirPermanentsEffect());
        addEffect(EffectSlot.STATIC, new PreventHalfDamageToControllerAndTheirPermanentsEffect());
    }
}
