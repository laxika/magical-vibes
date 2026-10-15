package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "ODY", collectorNumber = "15")
public class Confessor extends Card {

    public Confessor() {
        addEffect(EffectSlot.ON_OPPONENT_DISCARDS, new MayEffect(new GainLifeEffect(1), "Gain 1 life?"));
        addEffect(EffectSlot.ON_CONTROLLER_DISCARDS, new MayEffect(new GainLifeEffect(1), "Gain 1 life?"));
    }
}
