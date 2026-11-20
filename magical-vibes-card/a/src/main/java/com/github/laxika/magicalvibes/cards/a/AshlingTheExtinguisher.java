package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByTriggeringPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "EVE", collectorNumber = "33")
public class AshlingTheExtinguisher extends Card {

    public AshlingTheExtinguisher() {
        // Whenever Ashling deals combat damage to a player, choose target creature that
        // player controls. The player sacrifices that creature.
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new SacrificeSelfEffect(Subjects.target(), Subjects.triggeringPlayer(),
                        new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(),
                                new PermanentControlledByTriggeringPlayerPredicate()))));
    }
}
