package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

@CardRegistration(set = "M11", collectorNumber = "139")
@CardRegistration(set = "DKA", collectorNumber = "90")
public class Fling extends Card {

    public Fling() {
        // The sacrifice cost snapshots the sacrificed creature's power into the entry's xValue.
        addEffect(EffectSlot.SPELL, new SacrificeCreatureCost(false, true));
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(new XValue()));
    }
}
