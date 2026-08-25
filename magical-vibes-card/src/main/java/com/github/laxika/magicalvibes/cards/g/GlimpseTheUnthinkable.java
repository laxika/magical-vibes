package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "RAV", collectorNumber = "208")
public class GlimpseTheUnthinkable extends Card {

    public GlimpseTheUnthinkable() {
        // Target player mills ten cards.
        addEffect(EffectSlot.SPELL, new MillEffect(10, MillRecipient.TARGET_PLAYER));
    }
}
