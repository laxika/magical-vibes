package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentsOnCombatDamageToPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BOK", collectorNumber = "43")
@CardRegistration(set = "PC2", collectorNumber = "20")
@CardRegistration(set = "PCA", collectorNumber = "20")
public class MistbladeShinobi extends Card {

    public MistbladeShinobi() {
        // Ninjutsu {U}
        addNinjutsu("{U}");

        // Whenever this creature deals combat damage to a player, you may return target
        // creature that player controls to its owner's hand.
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new ReturnPermanentsOnCombatDamageToPlayerEffect(new PermanentIsCreaturePredicate(), 1));
    }
}
