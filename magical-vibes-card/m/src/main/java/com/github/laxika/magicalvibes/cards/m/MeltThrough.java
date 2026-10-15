package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageNotRemovedDuringCleanupEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetThenPerpetuallyGrantStaticEffectIfCreatureEffect;

@CardRegistration(set = "YBRO", collectorNumber = "10")
public class MeltThrough extends Card {

    public MeltThrough() {
        addEffect(EffectSlot.SPELL,
                new DealDamageToAnyTargetThenPerpetuallyGrantStaticEffectIfCreatureEffect(
                        2, new DamageNotRemovedDuringCleanupEffect()));
    }
}
