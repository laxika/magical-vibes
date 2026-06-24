package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAttackedTargetEffect;

@CardRegistration(set = "DKA", collectorNumber = "93")
public class Hellrider extends Card {

    public Hellrider() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS, new DealDamageToAttackedTargetEffect(1));
    }
}
