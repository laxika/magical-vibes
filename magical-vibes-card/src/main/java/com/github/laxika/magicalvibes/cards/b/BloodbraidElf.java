package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;

@CardRegistration(set = "ARB", collectorNumber = "50")
public class BloodbraidElf extends Card {

    public BloodbraidElf() {
        // Cascade: when you cast this spell, dig the library until a nonland card with lesser mana
        // value, may cast it for free, rest to the bottom in a random order (CascadeEffectHandler).
        // (Haste is Scryfall-loaded.)
        addEffect(EffectSlot.ON_SELF_CAST, new CascadeEffect());
    }
}
