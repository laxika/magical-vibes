package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "ARB", collectorNumber = "66")
public class CapturedSunlight extends Card {

    public CapturedSunlight() {
        // You gain 4 life.
        addEffect(EffectSlot.SPELL, new GainLifeEffect(4));

        // Cascade: when you cast this spell, dig the library until a nonland card with lesser mana
        // value, may cast it for free, rest to the bottom in a random order (CascadeEffectHandler).
        addEffect(EffectSlot.ON_SELF_CAST, new CascadeEffect());
    }
}
