package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "RIX", collectorNumber = "168")
public class RagingRegisaur extends Card {

    public RagingRegisaur() {
        // Whenever this creature attacks, it deals 1 damage to any target.
        addEffect(EffectSlot.ON_ATTACK, new DealDamageToAnyTargetEffect(1));
    }
}
