package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

@CardRegistration(set = "THS", collectorNumber = "173")
public class ReverentHunter extends Card {

    public ReverentHunter() {
        // When this creature enters, put +1/+1 counters on it equal to your green devotion.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new PutCountersOnSelfEffect(
                CounterType.PLUS_ONE_PLUS_ONE,
                new ColorManaSymbolsAmongControlledPermanents(ManaColor.GREEN)));
    }
}
