package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BOK", collectorNumber = "84")
@CardRegistration(set = "PHUK", collectorNumber = "4")
@CardRegistration(set = "PHUK", collectorNumber = "42")
@CardRegistration(set = "PHUK", collectorNumber = "43")
@CardRegistration(set = "PC2", collectorNumber = "36")
@CardRegistration(set = "PCA", collectorNumber = "36")
public class Skullsnatcher extends Card {

    public Skullsnatcher() {
        addNinjutsu("{B}");

        // "Whenever this creature deals combat damage to a player, exile up to two target cards from
        // that player's graveyard." The targets are chosen from the damaged player's graveyard as
        // the trigger goes on the stack; "up to two" allows choosing zero.
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new ExileCardsFromGraveyardEffect(2, 0));
    }
}
