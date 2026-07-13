package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnLandControlledByPlayerToHandEffect;

@CardRegistration(set = "7ED", collectorNumber = "85")
public class ManaBreach extends Card {

    public ManaBreach() {
        // Whenever a player casts a spell, that player returns a land they control to its owner's hand.
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, new ReturnLandControlledByPlayerToHandEffect());
    }
}
