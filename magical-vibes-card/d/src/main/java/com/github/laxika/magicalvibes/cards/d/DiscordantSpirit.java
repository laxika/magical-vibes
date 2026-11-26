package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DamageDealtToControllerThisTurn;
import com.github.laxika.magicalvibes.model.condition.NotControllerTurn;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersEffect;

@CardRegistration(set = "MIR", collectorNumber = "261")
public class DiscordantSpirit extends Card {

    public DiscordantSpirit() {
        // At the beginning of each end step, if it's an opponent's turn, put a +1/+1 counter on this
        // creature for each 1 damage dealt to you this turn.
        addTrigger(Triggers.endStep(ActivePlayer.ANY),
                TriggerOptions.DEFAULT.withInterveningIf(new NotControllerTurn()),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE, new DamageDealtToControllerThisTurn()));

        // At the beginning of your end step, remove all +1/+1 counters from this creature.
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                RemoveCountersEffect.allFromSelf(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
