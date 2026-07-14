package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;

@CardRegistration(set = "7ED", collectorNumber = "152")
public class Oppression extends Card {

    public Oppression() {
        // Whenever a player casts a spell, that player discards a card.
        // The ON_ANY_PLAYER_CASTS_SPELL collector stamps the casting player onto the entry's
        // targetId, so the TARGET_PLAYER discard lands on whoever cast the spell.
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER, false));
    }
}
