package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeEqualToPowerEffect;

@CardRegistration(set = "NPH", collectorNumber = "66")
public class MortisDogs extends Card {

    public MortisDogs() {
        addEffect(EffectSlot.ON_ATTACK, new BoostSelfEffect(2, 0));
        addEffect(EffectSlot.ON_DEATH, new TargetPlayerLosesLifeEqualToPowerEffect());
    }
}
