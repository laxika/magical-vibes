package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "USG", collectorNumber = "164")
public class VampiricEmbrace extends Card {

    public VampiricEmbrace() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(2, 2, Set.of(Keyword.FLYING), GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.ON_DAMAGED_CREATURE_DIES,
                        new PutCountersOnEnchantedCreatureEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));
    }
}
