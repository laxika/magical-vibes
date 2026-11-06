package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByTriggeringPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "22")
public class DawningPurist extends Card {

    public DawningPurist() {
        addMorph("{1}{W}");

        // Whenever this creature deals combat damage to a player, you may destroy target enchantment
        // that player controls.
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new MayEffect(new DestroyTargetPermanentEffect(new PermanentAllOfPredicate(List.of(
                        new PermanentIsEnchantmentPredicate(),
                        new PermanentControlledByTriggeringPlayerPredicate()))),
                        "You may destroy target enchantment that player controls."));
    }
}
