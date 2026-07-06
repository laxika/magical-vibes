package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "DKA", collectorNumber = "37")
public class GeralfsMindcrusher extends Card {

    public GeralfsMindcrusher() {
        // When Geralf's Mindcrusher enters the battlefield, target player mills five cards.
        // Undying is loaded from Scryfall and resolved automatically when it dies.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MillEffect(5, MillRecipient.TARGET_PLAYER));
    }
}
