package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;

@CardRegistration(set = "DKA", collectorNumber = "37")
public class GeralfsMindcrusher extends Card {

    public GeralfsMindcrusher() {
        // When Geralf's Mindcrusher enters the battlefield, target player mills five cards.
        // Undying is loaded from Scryfall and resolved automatically when it dies.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MillTargetPlayerEffect(5));
    }
}
