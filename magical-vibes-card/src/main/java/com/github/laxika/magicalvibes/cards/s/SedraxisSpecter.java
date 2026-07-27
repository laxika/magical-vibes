package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;


@CardRegistration(set = "ALA", collectorNumber = "192")
public class SedraxisSpecter extends Card {

    public SedraxisSpecter() {
        // Flying is an auto-loaded keyword.

        // Whenever this creature deals combat damage to a player, that player discards a card.
        // The engine routes TARGET_PLAYER discards on this slot to the damaged player.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER, false));

        // Unearth {1}{B}: Return this card from your graveyard to the battlefield. It gains haste.
        // Exile it at the beginning of the next end step. Unearth only as a sorcery.
        addUnearth("{1}{B}");
    }
}
