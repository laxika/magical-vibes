package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllPermanentsUpkeepSacrificeUnlessPayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "WTH", collectorNumber = "47")
public class PendrellMists extends Card {

    public PendrellMists() {
        // All creatures have "At the beginning of your upkeep, sacrifice this creature unless you pay {1}."
        addEffect(EffectSlot.STATIC,
                new AllPermanentsUpkeepSacrificeUnlessPayEffect(new PermanentIsCreaturePredicate(), "{1}"));
    }
}
