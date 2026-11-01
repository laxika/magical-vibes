package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SNC", collectorNumber = "153")
public class MostWanted extends Card {

    public MostWanted() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(2, 1, GrantScope.ENCHANTED_CREATURE))
                .addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                        CreateTokenEffect.ofTreasureToken(2));
    }
}
