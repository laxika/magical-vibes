package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentControlledByPlayerToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EVE", collectorNumber = "18")
@CardRegistration(set = "DDI", collectorNumber = "18")
public class CacheRaiders extends Card {

    public CacheRaiders() {
        // At the beginning of your upkeep, return a permanent you control to its owner's hand.
        // Non-targeting choice at resolution; there is always at least this creature to return.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new ReturnPermanentControlledByPlayerToHandEffect(
                new PermanentTruePredicate(),
                "permanent"
        ));
    }
}
