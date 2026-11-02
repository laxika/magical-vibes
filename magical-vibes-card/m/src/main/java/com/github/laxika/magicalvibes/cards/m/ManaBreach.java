package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentControlledByPlayerToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "7ED", collectorNumber = "85")
@CardRegistration(set = "EXO", collectorNumber = "38")
public class ManaBreach extends Card {

    public ManaBreach() {
        // Whenever a player casts a spell, that player returns a land they control to its owner's hand.
        addTrigger(Triggers.playerCastsSpell(),
                new ReturnPermanentControlledByPlayerToHandEffect(new PermanentIsLandPredicate(), "land"));
    }
}
