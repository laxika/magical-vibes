package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageDealtByEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TSP", collectorNumber = "43")
@CardRegistration(set = "TSR", collectorNumber = "48")
public class TemporalIsolation extends Card {

    public TemporalIsolation() {
        target(TargetFilters.creature())
                .addStaticEffect(new GrantKeywordEffect(Keyword.SHADOW, GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new PreventAllDamageDealtByEnchantedCreatureEffect());
    }
}
