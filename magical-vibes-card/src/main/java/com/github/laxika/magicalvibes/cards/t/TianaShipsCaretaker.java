package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnCardFromGraveyardToHandEffect;

@CardRegistration(set = "DOM", collectorNumber = "208")
public class TianaShipsCaretaker extends Card {

    public TianaShipsCaretaker() {
        // Flying, first strike — loaded from Scryfall
        // Whenever an Aura or Equipment you control is put into a graveyard from the battlefield,
        // you may return that card to its owner's hand at the beginning of the next end step.
        addEffect(EffectSlot.ON_ALLY_AURA_OR_EQUIPMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                new RegisterDelayedReturnCardFromGraveyardToHandEffect(null));
    }
}
