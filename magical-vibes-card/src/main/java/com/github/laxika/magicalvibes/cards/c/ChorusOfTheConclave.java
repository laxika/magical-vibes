package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreatureSpellAdditionalCountersCostEffect;

@CardRegistration(set = "RAV", collectorNumber = "195")
public class ChorusOfTheConclave extends Card {

    public ChorusOfTheConclave() {
        addEffect(EffectSlot.STATIC, new CreatureSpellAdditionalCountersCostEffect());
    }
}
