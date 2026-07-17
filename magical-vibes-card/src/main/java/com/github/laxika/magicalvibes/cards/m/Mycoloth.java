package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DevourEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ALA", collectorNumber = "140")
public class Mycoloth extends Card {

    public Mycoloth() {
        // Devour 2 (As this creature enters, you may sacrifice any number of creatures.
        // It enters with twice that many +1/+1 counters on it.)
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DevourEffect(2));

        // At the beginning of your upkeep, create a 1/1 green Saproling creature token
        // for each +1/+1 counter on this creature.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CreateTokenEffect(
                new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE),
                "Saproling",
                1,
                1,
                CardColor.GREEN,
                List.of(CardSubtype.SAPROLING),
                Set.of(),
                Set.of()
        ));
    }
}
