package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAllMatchingPermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "10E", collectorNumber = "236")
@CardRegistration(set = "CHK", collectorNumber = "190")
public class Soulblast extends Card {

    public Soulblast() {
        // The sacrifice cost snapshots the creatures' total power into the entry's xValue.
        addCastingCost(new SacrificeAllMatchingPermanentsCost(new PermanentIsCreaturePredicate(), true));
        addSpellEffect(DealDamageEffect.toAnyTarget(new XValue()));
    }
}
