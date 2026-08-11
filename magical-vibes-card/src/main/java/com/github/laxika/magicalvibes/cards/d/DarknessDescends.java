package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "ECL", collectorNumber = "97")
public class DarknessDescends extends Card {

    public DarknessDescends() {
        addEffect(EffectSlot.SPELL, new PutCounterOnEachMatchingPermanentEffect(
                CounterType.MINUS_ONE_MINUS_ONE, 2,
                new PermanentIsCreaturePredicate(), EachPermanentScope.ALL_PLAYERS));
    }
}
