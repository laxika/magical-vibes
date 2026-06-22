package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoubleDamageToEnchantedPlayerEffect;

@CardRegistration(set = "DKA", collectorNumber = "85")
public class CurseOfBloodletting extends Card {

    public CurseOfBloodletting() {
        addEffect(EffectSlot.STATIC, new DoubleDamageToEnchantedPlayerEffect());
    }
}
