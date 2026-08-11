package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllPermanentsUpkeepSacrificeUnlessPayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "INV", collectorNumber = "285")
public class VileConsumption extends Card {

    public VileConsumption() {
        // All creatures have "At the beginning of your upkeep, sacrifice this creature unless you pay 1 life."
        addEffect(EffectSlot.STATIC,
                new AllPermanentsUpkeepSacrificeUnlessPayEffect(new PermanentIsCreaturePredicate(), 1));
    }
}
