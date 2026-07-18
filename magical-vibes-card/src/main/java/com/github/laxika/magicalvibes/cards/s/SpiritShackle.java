package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "4ED", collectorNumber = "163")
public class SpiritShackle extends Card {

    public SpiritShackle() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ));
        // Whenever enchanted creature becomes tapped, put a -0/-2 counter on it.
        addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_TAPPED,
                new PutCounterOnEnchantedCreatureEffect(CounterType.MINUS_ZERO_MINUS_TWO));
    }
}
