package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "OGW", collectorNumber = "34")
@CardRegistration(set = "PIO", collectorNumber = "362")
@CardRegistration(set = "E01", collectorNumber = "20")
public class ShoulderToShoulder extends Card {

    public ShoulderToShoulder() {
        target(TargetFilters.creature(), 0, 2)
                .addSpellEffect(new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE));
        addSpellEffect(new DrawCardEffect(1));
    }
}
