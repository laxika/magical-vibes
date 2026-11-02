package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EVE", collectorNumber = "33")
public class AshlingTheExtinguisher extends Card {

    public AshlingTheExtinguisher() {
        // Whenever Ashling deals combat damage to a player, choose target creature that
        // player controls. The player sacrifices that creature.
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new SacrificePermanentDamagedPlayerControlsEffect(new PermanentIsCreaturePredicate(), 0));
    }
}
