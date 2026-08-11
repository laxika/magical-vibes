package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAttackedTargetEffect;

@CardRegistration(set = "M20", collectorNumber = "159")
public class ScorchSpitter extends Card {

    public ScorchSpitter() {
        addEffect(EffectSlot.ON_ATTACK, new DealDamageToAttackedTargetEffect(1));
    }
}
