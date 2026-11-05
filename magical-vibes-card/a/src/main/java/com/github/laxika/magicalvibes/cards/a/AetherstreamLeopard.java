package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerEnergyAtLeast;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AER", collectorNumber = "103")
public class AetherstreamLeopard extends Card {

    public AetherstreamLeopard() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new EnergyCountersEffect(1));

        addTrigger(Triggers.attacks(), new MayEffect(
                new ConditionalEffect(new ControllerEnergyAtLeast(1),
                        SequenceEffect.of(
                                new EnergyCountersEffect(-1),
                                new BoostSelfEffect(2, 0)
                        )),
                "Pay {E} to give Aetherstream Leopard +2/+0 until end of turn?"
        ));
    }
}
