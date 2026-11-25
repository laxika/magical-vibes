package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "PCY", collectorNumber = "129")
@CardRegistration(set = "DIS", collectorNumber = "98")
@CardRegistration(set = "MM2", collectorNumber = "166")
@CardRegistration(set = "2X2", collectorNumber = "161")
public class Thrive extends Card {

    public Thrive() {
        targetExactlyX(TargetFilters.creature(), 100)
                .addSpellEffect(new PutCountersEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
