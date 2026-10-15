package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.condition.ControllerEnergyAtLeast;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GainEnergyEqualToExcessCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "M3C", collectorNumber = "58")
@CardRegistration(set = "M3C", collectorNumber = "110")
public class OverclockedElectromancer extends Card {

    public OverclockedElectromancer() {
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new MayEffect(
                ConditionalEffect.unless(new ControllerEnergyAtLeast(3),
                        SequenceEffect.of(
                                new EnergyCountersEffect(-3),
                                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 1)
                        )),
                "Pay {E}{E}{E} to put a +1/+1 counter on Overclocked Electromancer?"
        ));

        addEffect(EffectSlot.ON_ATTACK,
                new BoostSelfEffect(new SourcePower(), new Fixed(0)));

        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_CREATURE,
                new GainEnergyEqualToExcessCombatDamageEffect());
    }
}
