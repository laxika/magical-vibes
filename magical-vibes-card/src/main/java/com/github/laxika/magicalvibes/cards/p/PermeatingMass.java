package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomeTargetPermanentCopyOfSourceEffect;

@CardRegistration(set = "EMN", collectorNumber = "165")
public class PermeatingMass extends Card {

    public PermeatingMass() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_CREATURE, new BecomeTargetPermanentCopyOfSourceEffect());
    }
}
