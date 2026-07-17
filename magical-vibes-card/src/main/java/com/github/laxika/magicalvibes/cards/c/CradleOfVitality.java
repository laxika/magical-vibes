package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CradleOfVitalityLifeGainEffect;

@CardRegistration(set = "ALA", collectorNumber = "7")
public class CradleOfVitality extends Card {

    public CradleOfVitality() {
        // Whenever you gain life, you may pay {1}{W}. If you do, put a +1/+1 counter on target
        // creature for each 1 life you gained.
        addEffect(EffectSlot.ON_CONTROLLER_GAINS_LIFE, new CradleOfVitalityLifeGainEffect("{1}{W}"));
    }
}
