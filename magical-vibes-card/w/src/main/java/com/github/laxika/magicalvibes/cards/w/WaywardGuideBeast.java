package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentControlledByPlayerToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZNR", collectorNumber = "176")
public class WaywardGuideBeast extends Card {

    public WaywardGuideBeast() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                ReturnPermanentControlledByPlayerToHandEffect.controller(
                        new PermanentIsLandPredicate(), "land"));
    }
}
