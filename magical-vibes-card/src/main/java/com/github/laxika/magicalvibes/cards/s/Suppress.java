package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPlayerHandFaceDownAndReturnAtNextTurnEndStepEffect;

@CardRegistration(set = "APC", collectorNumber = "52")
public class Suppress extends Card {

    public Suppress() {
        addEffect(EffectSlot.SPELL, new ExileTargetPlayerHandFaceDownAndReturnAtNextTurnEndStepEffect());
    }
}
