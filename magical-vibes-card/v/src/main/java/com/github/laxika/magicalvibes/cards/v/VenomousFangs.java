package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "USG", collectorNumber = "280")
public class VenomousFangs extends Card {

    public VenomousFangs() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.enchantedCreatureDealsDamageToCreature(),
                        new DestroyTargetPermanentEffect());
    }
}
