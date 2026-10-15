package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "CSP", collectorNumber = "7")
public class GlacialPlating extends Card {

    public GlacialPlating() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.UPKEEP_TRIGGERED, new CumulativeUpkeepEffect("{S}"))
                .addEffect(EffectSlot.STATIC,
                        new StaticBoostEffect(3, 3, GrantScope.ENCHANTED_CREATURE, CounterType.AGE));
    }
}
