package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ShuffleGraveyardIntoLibraryEffect;

public class Reminisce extends Card {

    public Reminisce() {
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new ShuffleGraveyardIntoLibraryEffect());
    }
}
