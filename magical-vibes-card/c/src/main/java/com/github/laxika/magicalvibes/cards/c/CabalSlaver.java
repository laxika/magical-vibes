package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONS", collectorNumber = "131")
public class CabalSlaver extends Card {

    public CabalSlaver() {
        // Whenever a Goblin deals combat damage to a player, that player discards a card.
        // The trigger is Cabal Slaver's own, so its controller controls it whoever controls the Goblin.
        addTrigger(Triggers.creatureDealsCombatDamageToYou(),
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.GOBLIN),
                        new DiscardEffect(1, DiscardRecipient.CONTROLLER)));
        addTrigger(Triggers.creatureDealsCombatDamageToOpponent(),
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.GOBLIN),
                        new DiscardEffect(1, DiscardRecipient.TRIGGERING_PLAYER)));
    }
}
