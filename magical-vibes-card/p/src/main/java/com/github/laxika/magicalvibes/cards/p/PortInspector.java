package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MMQ", collectorNumber = "90")
public class PortInspector extends Card {

    public PortInspector() {
        addTrigger(Triggers.becomesBlocked(),
                new MayEffect(RevealCardsFromHandEffect.hand(Subjects.defendingPlayer()).seenOnlyBy(Subjects.you()), "Look at defending player's hand?"));
    }
}
