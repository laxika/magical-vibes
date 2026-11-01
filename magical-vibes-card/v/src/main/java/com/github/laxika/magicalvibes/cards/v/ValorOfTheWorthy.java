package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KHM", collectorNumber = "37")
public class ValorOfTheWorthy extends Card {

    public ValorOfTheWorthy() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.ENCHANTED_CREATURE))
                .addTrigger(Triggers.leavesBattlefield(null, ObjectRelation.ENCHANTED),
                        CreateTokenEffect.whiteSpirit(1));
    }
}
