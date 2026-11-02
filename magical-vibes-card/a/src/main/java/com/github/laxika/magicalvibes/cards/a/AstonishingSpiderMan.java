package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsDiscardedOrCycledThisTurn;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandThenDrawEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "740")
public class AstonishingSpiderMan extends Card {

    public AstonishingSpiderMan() {
        addTrigger(Triggers.attacks(), new MayEffect(
                new DiscardOwnHandThenDrawEffect(new CardsDiscardedOrCycledThisTurn()),
                "Discard your hand and draw a card for each card you've discarded this turn?"));
    }
}
