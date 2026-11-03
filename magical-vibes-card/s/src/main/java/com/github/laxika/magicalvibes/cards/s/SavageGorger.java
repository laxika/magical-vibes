package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.OpponentLostLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M20", collectorNumber = "291")
@CardRegistration(set = "ANB", collectorNumber = "58")
public class SavageGorger extends Card {

    public SavageGorger() {
        // At the beginning of your end step, if an opponent lost life this turn,
        // put a +1/+1 counter on this creature.
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new OpponentLostLifeThisTurn(1)),
                new PutCountersOnSourceEffect(1, 1, 1));
    }
}
