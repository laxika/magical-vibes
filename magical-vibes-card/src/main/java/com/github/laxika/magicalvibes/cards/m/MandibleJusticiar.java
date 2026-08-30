package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "ONE", collectorNumber = "21")
public class MandibleJusticiar extends Card {

    public MandibleJusticiar() {
        // Whenever another artifact you control enters, this creature gets +1/+1 until end of turn.
        addEffect(EffectSlot.ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD, new BoostSelfEffect(1, 1));
    }
}
