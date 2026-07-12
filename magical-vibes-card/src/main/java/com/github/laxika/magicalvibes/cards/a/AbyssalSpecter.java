package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;

@CardRegistration(set = "8ED", collectorNumber = "117")
public class AbyssalSpecter extends Card {

    public AbyssalSpecter() {
        // Flying comes from the Scryfall keyword data.
        // Whenever this creature deals damage to a player, that player (the damaged player)
        // discards a card. The engine routes TARGET_PLAYER discards on this slot to the damaged player.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER, false));
    }
}
