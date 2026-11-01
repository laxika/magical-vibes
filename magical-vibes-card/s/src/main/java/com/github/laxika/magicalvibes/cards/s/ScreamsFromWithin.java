package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceAuraToCreatureOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DST", collectorNumber = "52")
@CardRegistration(set = "SOC", collectorNumber = "119")
public class ScreamsFromWithin extends Card {

    public ScreamsFromWithin() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(-1, -1, GrantScope.ENCHANTED_CREATURE))
                .addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                        new ReturnSourceAuraToCreatureOnDeathEffect());
    }
}
