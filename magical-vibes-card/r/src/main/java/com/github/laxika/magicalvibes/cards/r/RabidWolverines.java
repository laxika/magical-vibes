package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "EXO", collectorNumber = "119")
public class RabidWolverines extends Card {

    public RabidWolverines() {
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new BoostSelfEffect(1, 1), TriggerMode.PER_BLOCKER);
    }
}
