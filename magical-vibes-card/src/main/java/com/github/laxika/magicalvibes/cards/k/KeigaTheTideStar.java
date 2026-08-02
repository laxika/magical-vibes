package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;

@CardRegistration(set = "CHK", collectorNumber = "72")
public class KeigaTheTideStar extends Card {

    public KeigaTheTideStar() {
        // When Keiga dies, gain control of target creature.
        addEffect(EffectSlot.ON_DEATH, new GainControlOfTargetEffect(ControlDuration.PERMANENT));
    }
}
