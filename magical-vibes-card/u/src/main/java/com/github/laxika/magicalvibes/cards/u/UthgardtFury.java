package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageNotRemovedFromOpponentsCreaturesDuringCleanupEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "HBG", collectorNumber = "63")
public class UthgardtFury extends Card {

    public UthgardtFury() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DealDamageToAnyTargetEffect(4));
        addEffect(EffectSlot.STATIC, new DamageNotRemovedFromOpponentsCreaturesDuringCleanupEffect());
    }
}
