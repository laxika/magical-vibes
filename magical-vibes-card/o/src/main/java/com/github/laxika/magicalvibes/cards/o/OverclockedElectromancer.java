package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.condition.ControllerEnergyAtLeast;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M3C", collectorNumber = "58")
@CardRegistration(set = "M3C", collectorNumber = "110")
public class OverclockedElectromancer extends Card {

    public OverclockedElectromancer() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU), new MayEffect(
                new ConditionalEffect(new ControllerEnergyAtLeast(3),
                        SequenceEffect.of(
                                new EnergyCountersEffect(-3),
                                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE, 1)
                        )),
                "Pay {E}{E}{E} to put a +1/+1 counter on Overclocked Electromancer?"
        ));

        addTrigger(Triggers.attacks(),
                BoostEffect.self(new SourcePower(), new Fixed(0)));

        addTrigger(Triggers.dealsCombatDamageToCreature(),
                TriggerOptions.DEFAULT.withInterveningIf(new EventValueAtLeast(1)),
                new EnergyCountersEffect(new EventValue()));
    }
}
