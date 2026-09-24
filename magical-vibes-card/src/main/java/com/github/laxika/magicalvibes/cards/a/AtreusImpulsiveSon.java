package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.ControllerExperienceCounters;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "2212")
public class AtreusImpulsiveSon extends Card {

    public AtreusImpulsiveSon() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(
                        new DrawCardEffect(new ControllerExperienceCounters()),
                        new DiscardEffect(1, DiscardRecipient.CONTROLLER),
                        new DealDamageToPlayersEffect(2, DamageRecipient.EACH_OPPONENT)
                ),
                "{3}, {T}: Draw a card for each experience counter you have, then discard a card. "
                        + "Atreus deals 2 damage to each opponent."
        ));
    }
}
