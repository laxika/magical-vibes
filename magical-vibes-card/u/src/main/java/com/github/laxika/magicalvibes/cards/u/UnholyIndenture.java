package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedCreatureToBattlefieldOnDeathEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M20", collectorNumber = "119")
public class UnholyIndenture extends Card {

    public UnholyIndenture() {
        // Enchant creature
        target(TargetFilters.creature());

        // When enchanted creature dies, return that card to the battlefield under your control with a +1/+1 counter on it.
        addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                new ReturnEnchantedCreatureToBattlefieldOnDeathEffect(true, CounterType.PLUS_ONE_PLUS_ONE));
    }
}
