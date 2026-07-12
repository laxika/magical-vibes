package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

@CardRegistration(set = "9ED", collectorNumber = "225")
@CardRegistration(set = "8ED", collectorNumber = "230")
public class ViashinoSandstalker extends Card {

    public ViashinoSandstalker() {
        // Haste is auto-loaded from Scryfall keywords.
        addEffect(EffectSlot.END_STEP_TRIGGERED, ReturnToHandEffect.self());
    }
}
