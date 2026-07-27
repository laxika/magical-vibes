package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;


@CardRegistration(set = "CON", collectorNumber = "51")
public class RottingRats extends Card {

    public RottingRats() {
        // When this creature enters, each player discards a card (APNAP order).
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new DiscardEffect(1, DiscardRecipient.EACH_PLAYER));

        // Unearth {1}{B}: Return this card from your graveyard to the battlefield. It gains haste.
        // Exile it at the beginning of the next end step. Unearth only as a sorcery.
        addUnearth("{1}{B}");
    }
}
