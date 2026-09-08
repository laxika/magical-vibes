package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerEnergyAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "AER", collectorNumber = "62")
public class GlintSleeveSiphoner extends Card {

    public GlintSleeveSiphoner() {
        EnergyCountersEffect gainEnergy = new EnergyCountersEffect(1);
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, gainEnergy);
        addEffect(EffectSlot.ON_ATTACK, gainEnergy);

        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                ConditionalEffect.unless(new ControllerEnergyAtLeast(2),
                        SequenceEffect.of(
                                new EnergyCountersEffect(-2),
                                new DrawCardEffect(1),
                                new LoseLifeEffect(1))),
                "Pay {E}{E} to draw a card and lose 1 life?"));
    }
}
