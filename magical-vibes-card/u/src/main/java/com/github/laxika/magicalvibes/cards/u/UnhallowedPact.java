package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedCreatureToBattlefieldOnDeathEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AVR", collectorNumber = "124")
@CardRegistration(set = "DDR", collectorNumber = "64")
public class UnhallowedPact extends Card {

    public UnhallowedPact() {
        // Enchant creature
        target(TargetFilters.creature());

        // When enchanted creature dies, return that card to the battlefield under your control.
        addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                new ReturnEnchantedCreatureToBattlefieldOnDeathEffect(true));
    }
}
