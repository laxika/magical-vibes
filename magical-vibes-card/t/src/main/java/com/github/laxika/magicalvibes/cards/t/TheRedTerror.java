package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceCardForColorSourceDamageEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "40K", collectorNumber = "83")
public class TheRedTerror extends Card {

    public TheRedTerror() {
        addTrigger(Triggers.sourceDealsDamage(),
                new PutCountersOnSourceCardForColorSourceDamageEffect(
                        CardColor.RED, CounterType.PLUS_ONE_PLUS_ONE));
    }
}
