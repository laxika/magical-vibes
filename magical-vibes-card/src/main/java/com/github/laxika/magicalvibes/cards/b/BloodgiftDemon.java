package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeEffect;

@CardRegistration(set = "ISD", collectorNumber = "89")
public class BloodgiftDemon extends Card {

    public BloodgiftDemon() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new DrawCardForTargetPlayerEffect(1, false, true));
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new TargetPlayerLosesLifeEffect(1));
    }
}
