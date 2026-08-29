package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerEnergyAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AER", collectorNumber = "26")
public class AetherSwooper extends Card {

    public AetherSwooper() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnergyCountersEffect(2));

        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                ConditionalEffect.unless(new ControllerEnergyAtLeast(2),
                        SequenceEffect.of(
                                new EnergyCountersEffect(-2),
                                new CreateTokenEffect(1, "Servo", 1, 1, null,
                                        List.of(CardSubtype.SERVO), Set.of(), Set.of(CardType.ARTIFACT))
                        )),
                "Pay {E}{E} to create a 1/1 colorless Servo artifact creature token?"
        ));
    }
}
