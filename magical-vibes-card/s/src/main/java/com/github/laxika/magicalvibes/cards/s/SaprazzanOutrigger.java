package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutSelfOnTopOfLibraryAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "101")
public class SaprazzanOutrigger extends Card {

    public SaprazzanOutrigger() {
        addTrigger(List.of(Triggers.attacks(), Triggers.blocks()), TriggerOptions.ONE_OR_MORE,
                new PutSelfOnTopOfLibraryAtEndOfCombatEffect());
    }
}
