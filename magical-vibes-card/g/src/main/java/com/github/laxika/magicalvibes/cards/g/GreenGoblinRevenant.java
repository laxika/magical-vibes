package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsDiscardedOrCycledThisTurn;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SPM", collectorNumber = "130")
@CardRegistration(set = "OM1", collectorNumber = "149")
public class GreenGoblinRevenant extends Card {

    public GreenGoblinRevenant() {
        addTrigger(Triggers.attacks(), SequenceEffect.of(
                new DiscardEffect(1, DiscardRecipient.CONTROLLER),
                new DrawCardEffect(new CardsDiscardedOrCycledThisTurn())
        ));
    }
}
