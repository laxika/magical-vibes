package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MorbidConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "DKA", collectorNumber = "116")
public class GravetillerWurm extends Card {

    public GravetillerWurm() {
        // Morbid - Gravetiller Wurm enters the battlefield with four +1/+1 counters on it
        // if a creature died this turn.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MorbidConditionalEffect(
                new PutCountersOnSourceEffect(1, 1, 4)
        ));
    }
}
