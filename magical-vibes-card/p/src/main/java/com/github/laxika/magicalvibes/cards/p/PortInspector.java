package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LookAtDefendingPlayerHandEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MMQ", collectorNumber = "90")
public class PortInspector extends Card {

    public PortInspector() {
        addTrigger(Triggers.becomesBlocked(),
                new MayEffect(new LookAtDefendingPlayerHandEffect(), "Look at defending player's hand?"));
    }
}
