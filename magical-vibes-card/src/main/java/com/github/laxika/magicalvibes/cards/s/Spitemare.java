package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "EVE", collectorNumber = "147")
public class Spitemare extends Card {

    public Spitemare() {
        // Whenever this creature is dealt damage, it deals that much damage to any target.
        // The damage amount snapshots into xValue; the controller chooses the target when serviced.
        addEffect(EffectSlot.ON_DEALT_DAMAGE, new DealDamageToAnyTargetEffect(new XValue()));
    }
}
