package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "AVR", collectorNumber = "149")
public class PillarOfFlame extends Card {

    public PillarOfFlame() {
        // 2 damage; a creature dealt damage this way that would die this turn is exiled instead.
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(new Fixed(2), false, true));
    }
}
