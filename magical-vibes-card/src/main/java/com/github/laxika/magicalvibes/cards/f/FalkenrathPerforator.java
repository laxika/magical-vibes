package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAttackedTargetEffect;

@CardRegistration(set = "MID", collectorNumber = "136")
public class FalkenrathPerforator extends Card {

    public FalkenrathPerforator() {
        addEffect(EffectSlot.ON_ATTACK, new DealDamageToAttackedTargetEffect(1));
    }
}
