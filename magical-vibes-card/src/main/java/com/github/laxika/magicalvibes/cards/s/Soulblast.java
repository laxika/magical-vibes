package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAllCreaturesYouControlCost;

@CardRegistration(set = "10E", collectorNumber = "236")
public class Soulblast extends Card {

    public Soulblast() {
        // The sacrifice cost snapshots the creatures' total power into the entry's xValue.
        addEffect(EffectSlot.SPELL, new SacrificeAllCreaturesYouControlCost());
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(new XValue()));
    }
}
