package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "USG", collectorNumber = "164")
public class VampiricEmbrace extends Card {

    public VampiricEmbrace() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(2, 2, Set.of(Keyword.FLYING), GrantScope.ENCHANTED_CREATURE))
                .addTrigger(Triggers.creatureDies(ObjectRelation.DAMAGED_BY_ATTACHED),
                        new PutCountersOnEnchantedCreatureEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));
    }
}
