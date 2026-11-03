package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "CON", collectorNumber = "48")
@CardRegistration(set = "DSC", collectorNumber = "144")
public class KederektParasite extends Card {

    public KederektParasite() {
        // Whenever an opponent draws a card, if you control a red permanent,
        // you may have this creature deal 1 damage to that player.
        addTrigger(Triggers.opponentDrawsCard(),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new ControlsPermanent(new PermanentColorInPredicate(Set.of(CardColor.RED)))),
                new MayEffect(
                        new DealDamageToPlayersEffect(1, DamageRecipient.TRIGGERING_PLAYER),
                        "Have Kederekt Parasite deal 1 damage to that player?"
                ));
    }
}
