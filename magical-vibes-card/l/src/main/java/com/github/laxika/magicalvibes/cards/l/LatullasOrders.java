package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByTriggeringPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "PCY", collectorNumber = "96")
public class LatullasOrders extends Card {

    public LatullasOrders() {
        target(TargetFilters.creature());
        // Whenever enchanted creature deals combat damage to defending player, you may
        // destroy target artifact that player controls.
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new MayEffect(
                new DestroyTargetPermanentEffect(new PermanentAllOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentControlledByTriggeringPlayerPredicate()))),
                "You may destroy target artifact that player controls."));
    }
}
