package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutSelfOnTopOfLibraryAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MMQ", collectorNumber = "101")
public class SaprazzanOutrigger extends Card {

    public SaprazzanOutrigger() {
        addTrigger(Triggers.attacks(), new PutSelfOnTopOfLibraryAtEndOfCombatEffect());
        addTrigger(Triggers.blocks(), new PutSelfOnTopOfLibraryAtEndOfCombatEffect());
    }
}
