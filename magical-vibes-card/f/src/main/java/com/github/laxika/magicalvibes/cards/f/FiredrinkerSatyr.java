package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "122")
public class FiredrinkerSatyr extends Card {

    public FiredrinkerSatyr() {
        addTrigger(Triggers.dealtDamage(),
                DealDamageEffect.to(Subjects.you(), new EventValue()));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(
                        BoostEffect.self(1, 0),
                        DealDamageEffect.to(Subjects.you(), 1)
                ),
                "{1}{R}: This creature gets +1/+0 until end of turn and deals 1 damage to you."
        ));
    }
}
