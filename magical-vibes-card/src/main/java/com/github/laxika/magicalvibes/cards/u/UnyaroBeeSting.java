package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "MIR", collectorNumber = "250")
public class UnyaroBeeSting extends Card {

    public UnyaroBeeSting() {
        // "Unyaro Bee Sting deals 2 damage to any target."
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(2));
    }
}
