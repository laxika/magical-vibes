package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByTriggeringPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "UDS", collectorNumber = "46")
@CardRegistration(set = "DDI", collectorNumber = "25")
public class SigilOfSleep extends Card {

    public SigilOfSleep() {
        target(TargetFilters.creature())
                // Whenever enchanted creature deals damage to a player, return target creature that
                // player controls to its owner's hand.
                .addTrigger(Triggers.dealsDamageToPlayer(),
                        ReturnToHandEffect.target(new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentControlledByTriggeringPlayerPredicate()))));
    }
}
