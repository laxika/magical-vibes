package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAttackedTargetEffect;

@CardRegistration(set = "RNA", collectorNumber = "198")
public class RakdosRoustabout extends Card {

    public RakdosRoustabout() {
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new DealDamageToAttackedTargetEffect(1));
    }
}
