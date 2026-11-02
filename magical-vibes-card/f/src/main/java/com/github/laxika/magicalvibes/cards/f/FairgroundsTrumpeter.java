package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.PlusOnePlusOneCounterPutOnControlledPermanentThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KLD", collectorNumber = "155")
@CardRegistration(set = "MOM", collectorNumber = "335")
public class FairgroundsTrumpeter extends Card {

    public FairgroundsTrumpeter() {
        addTrigger(Triggers.endStep(ActivePlayer.ANY), new ConditionalEffect(
                new PlusOnePlusOneCounterPutOnControlledPermanentThisTurn(),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)));
    }
}
