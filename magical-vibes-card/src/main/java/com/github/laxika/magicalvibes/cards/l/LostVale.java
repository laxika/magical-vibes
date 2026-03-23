package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;

/**
 * Lost Vale — back face of Dowsing Dagger.
 * Land.
 * (Transforms from Dowsing Dagger.)
 * {T}: Add three mana of any one color.
 */
public class LostVale extends Card {

    public LostVale() {
        // {T}: Add three mana of any one color.
        addEffect(EffectSlot.ON_TAP, new AwardAnyColorManaEffect(3));
    }
}
