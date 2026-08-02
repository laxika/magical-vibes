package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;

@CardRegistration(set = "GTC", collectorNumber = "78")
public class SlateStreetRuffian extends Card {

    public SlateStreetRuffian() {
        // Whenever this creature becomes blocked, defending player discards a card.
        addEffect(EffectSlot.ON_BECOMES_BLOCKED,
                new DiscardEffect(1, DiscardRecipient.DEFENDING_PLAYER));
    }
}
