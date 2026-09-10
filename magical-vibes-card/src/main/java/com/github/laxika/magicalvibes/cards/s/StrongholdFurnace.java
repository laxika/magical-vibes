package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleDamageEffect;

@CardRegistration(set = "OHOP", collectorNumber = "37")
public class StrongholdFurnace extends Card {

    public StrongholdFurnace() {
        addEffect(EffectSlot.STATIC, new DoubleDamageEffect());
        addEffect(EffectSlot.CHAOS_TRIGGERED, new DealDamageToAnyTargetEffect(1));
    }
}
