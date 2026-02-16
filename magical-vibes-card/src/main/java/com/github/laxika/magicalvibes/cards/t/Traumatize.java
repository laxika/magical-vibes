package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillHalfLibraryEffect;

public class Traumatize extends Card {

    public Traumatize() {
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new MillHalfLibraryEffect());
    }
}
