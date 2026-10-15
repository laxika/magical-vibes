package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileControllerHandFaceDownAndReturnAtNextEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextEndStepEffect;

@CardRegistration(set = "DIS", collectorNumber = "64")
public class IgnorantBliss extends Card {

    public IgnorantBliss() {
        addEffect(EffectSlot.SPELL, new ExileControllerHandFaceDownAndReturnAtNextEndStepEffect());
        addEffect(EffectSlot.SPELL, new RegisterDrawCardsAtNextEndStepEffect());
    }
}
