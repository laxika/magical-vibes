package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "8ED", collectorNumber = "139")
@CardRegistration(set = "MMQ", collectorNumber = "143")
@CardRegistration(set = "PHUK", collectorNumber = "49")
public class Larceny extends Card {

    public Larceny() {
        // Whenever a creature you control deals combat damage to a player, that player discards a card.
        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer(),
                new AllyCombatDamageTriggerEffect(
                        null,
                        new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER)));
    }
}
