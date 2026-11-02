package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "122")
public class FiredrinkerSatyr extends Card {

    public FiredrinkerSatyr() {
        addTrigger(Triggers.dealtDamage(),
                new DealDamageToPlayersEffect(new EventValue(), DamageRecipient.CONTROLLER));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(
                        new BoostSelfEffect(1, 0),
                        new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER)
                ),
                "{1}{R}: This creature gets +1/+0 until end of turn and deals 1 damage to you."
        ));
    }
}
