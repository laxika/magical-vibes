package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.DamageDealtToControllerThisTurn;
import com.github.laxika.magicalvibes.model.condition.NotControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersEffect;

@CardRegistration(set = "MIR", collectorNumber = "261")
public class DiscordantSpirit extends Card {

    public DiscordantSpirit() {
        // At the beginning of each end step, if it's an opponent's turn, put a +1/+1 counter on this
        // creature for each 1 damage dealt to you this turn.
        addEffect(EffectSlot.END_STEP_TRIGGERED, new ConditionalEffect(
                new NotControllerTurn(),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE,
                        new DamageDealtToControllerThisTurn())));

        // At the beginning of your end step, remove all +1/+1 counters from this creature.
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new RemoveAllCountersEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
