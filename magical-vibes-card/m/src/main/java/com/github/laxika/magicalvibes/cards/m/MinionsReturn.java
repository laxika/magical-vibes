package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedCreatureToBattlefieldOnDeathEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "THB", collectorNumber = "104")
public class MinionsReturn extends Card {

    public MinionsReturn() {
        target(TargetFilters.creature());

        addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                new ReturnEnchantedCreatureToBattlefieldOnDeathEffect(true));
    }
}
