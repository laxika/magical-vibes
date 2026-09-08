package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoubleDamageEffect;

@CardRegistration(set = "JOU", collectorNumber = "93")
public class DictateOfTheTwinGods extends Card {

    public DictateOfTheTwinGods() {
        addEffect(EffectSlot.STATIC, new DoubleDamageEffect());
    }
}
