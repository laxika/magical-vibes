package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

@CardRegistration(set = "ARB", collectorNumber = "19")
public class DenyReality extends Card {

    public DenyReality() {
        // Return target permanent to its owner's hand (any-permanent bounce; TargetCategory.PERMANENT
        // comes from ReturnToHandEffect.target()'s targetSpec, same as Boomerang).
        addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());

        // Cascade: when you cast this spell, dig the library until a nonland card with lesser mana
        // value, may cast it for free, rest to the bottom in a random order (CascadeEffectHandler).
        addEffect(EffectSlot.ON_SELF_CAST, new CascadeEffect());
    }
}
