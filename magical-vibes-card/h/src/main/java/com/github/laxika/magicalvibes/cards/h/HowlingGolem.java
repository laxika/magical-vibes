package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "218")
@CardRegistration(set = "GN2", collectorNumber = "54")
@CardRegistration(set = "GNT", collectorNumber = "53")
@CardRegistration(set = "GN3", collectorNumber = "117")
public class HowlingGolem extends Card {

    public HowlingGolem() {
        // Whenever this creature attacks or blocks, each player draws a card.
        addTrigger(List.of(Triggers.attacks(), Triggers.blocks()),
                TriggerOptions.ONE_OR_MORE,
                new EachPlayerDrawsCardEffect(1));
    }
}
