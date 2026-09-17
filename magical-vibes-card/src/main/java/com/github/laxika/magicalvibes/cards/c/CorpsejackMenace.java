package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoublePlusOnePlusOneCountersEffect;

@CardRegistration(set = "RTR", collectorNumber = "152")
@CardRegistration(set = "MB1", collectorNumber = "197")
@CardRegistration(set = "IMA", collectorNumber = "197")
@CardRegistration(set = "TMC", collectorNumber = "56")
public class CorpsejackMenace extends Card {

    public CorpsejackMenace() {
        // If one or more +1/+1 counters would be put on a creature you control, twice that many
        // +1/+1 counters are put on it instead.
        addEffect(EffectSlot.STATIC, new DoublePlusOnePlusOneCountersEffect());
    }
}
