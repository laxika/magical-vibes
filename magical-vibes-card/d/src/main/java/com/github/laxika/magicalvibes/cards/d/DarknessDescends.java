package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "ECL", collectorNumber = "97")
public class DarknessDescends extends Card {

    public DarknessDescends() {
        addSpellEffect(PutCountersEffect.onEach(CounterType.MINUS_ONE_MINUS_ONE, 2, new PermanentIsCreaturePredicate()));
    }
}
