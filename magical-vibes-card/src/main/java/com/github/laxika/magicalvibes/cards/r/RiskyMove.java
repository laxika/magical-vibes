package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RiskyMoveEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsControlOfSourceCreatureEffect;

@CardRegistration(set = "ONS", collectorNumber = "223")
public class RiskyMove extends Card {

    public RiskyMove() {
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, TargetPlayerGainsControlOfSourceCreatureEffect.triggeringPlayer());
        addEffect(EffectSlot.ON_SELF_BECOMES_CONTROLLED, new RiskyMoveEffect());
    }
}
