package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnCardFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DOM", collectorNumber = "208")
@CardRegistration(set = "DMR", collectorNumber = "204")
public class TianaShipsCaretaker extends Card {

    public TianaShipsCaretaker() {
        // Flying, first strike — loaded from Scryfall
        // Whenever an Aura or Equipment you control is put into a graveyard from the battlefield,
        // you may return that card to its owner's hand at the beginning of the next end step.
        addTrigger(Triggers.dies(Triggers.AURA_OR_EQUIPMENT, ObjectRelation.YOU_CONTROL),
                new RegisterDelayedReturnCardFromGraveyardToHandEffect());
    }
}
