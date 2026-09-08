package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerEnergyAtLeast;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "AER", collectorNumber = "103")
public class AetherstreamLeopard extends Card {

    public AetherstreamLeopard() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnergyCountersEffect(1));

        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                ConditionalEffect.unless(new ControllerEnergyAtLeast(1),
                        SequenceEffect.of(
                                new EnergyCountersEffect(-1),
                                new BoostSelfEffect(2, 0)
                        )),
                "Pay {E} to give Aetherstream Leopard +2/+0 until end of turn?"
        ));
    }
}
