package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeEffect;

@CardRegistration(set = "M10", collectorNumber = "111")
public class SanguineBond extends Card {

    public SanguineBond() {
        // "Whenever you gain life, target opponent loses that much life." The trigger collector
        // snapshots the life gained onto the entry's event value; EventValue reads it at resolution.
        addEffect(EffectSlot.ON_CONTROLLER_GAINS_LIFE, new TargetPlayerLosesLifeEffect(new EventValue()));
    }
}
