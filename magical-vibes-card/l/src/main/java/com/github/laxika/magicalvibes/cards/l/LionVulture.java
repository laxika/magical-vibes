package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.OpponentLostLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TLE", collectorNumber = "232")
public class LionVulture extends Card {

    public LionVulture() {
        // At the beginning of your end step, if an opponent lost life this turn, put a +1/+1
        // counter on this creature and draw a card.
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new OpponentLostLifeThisTurn(1)),
                SequenceEffect.of(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE), new DrawCardEffect(1)));
    }
}
