package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PayAnyAmountOfEnergyToPutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DRC", collectorNumber = "15")
@CardRegistration(set = "DRC", collectorNumber = "31")
public class RampagingAetherhood extends Card {

    public RampagingAetherhood() {
        // Ward {2}.
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL, new CounterUnlessPaysEffect(2));

        // The energy gain and the optional payment are one ability, so they resolve in order.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), SequenceEffect.of(
                new EnergyCountersEffect(new SourcePower()),
                new PayAnyAmountOfEnergyToPutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)));
    }
}
