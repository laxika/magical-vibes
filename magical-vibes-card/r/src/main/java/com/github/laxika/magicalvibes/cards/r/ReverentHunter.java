package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "THS", collectorNumber = "173")
@CardRegistration(set = "PIO", collectorNumber = "302")
public class ReverentHunter extends Card {

    public ReverentHunter() {
        // When this creature enters, put +1/+1 counters on it equal to your green devotion.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new PutCountersOnSelfEffect(
                CounterType.PLUS_ONE_PLUS_ONE,
                new ColorManaSymbolsAmongControlledPermanents(ManaColor.GREEN)));
    }
}
