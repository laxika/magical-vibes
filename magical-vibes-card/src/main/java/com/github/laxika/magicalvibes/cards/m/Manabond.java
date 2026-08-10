package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutAllLandsFromHandAndDiscardEffect;

@CardRegistration(set = "EXO", collectorNumber = "113")
public class Manabond extends Card {

    public Manabond() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new MayEffect(
                new PutAllLandsFromHandAndDiscardEffect(),
                "Reveal your hand and put all land cards from it onto the battlefield?"));
    }
}
