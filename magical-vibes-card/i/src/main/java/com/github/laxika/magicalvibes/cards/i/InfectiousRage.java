package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceAuraToRandomCreatureOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "JUD", collectorNumber = "92")
public class InfectiousRage extends Card {

    public InfectiousRage() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(2, -1, GrantScope.ENCHANTED_CREATURE))
                .addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                        new ReturnSourceAuraToRandomCreatureOnDeathEffect());
    }
}
