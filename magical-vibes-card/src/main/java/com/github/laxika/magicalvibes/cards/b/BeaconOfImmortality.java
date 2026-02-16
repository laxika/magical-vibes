package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoubleTargetPlayerLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;

public class BeaconOfImmortality extends Card {

    public BeaconOfImmortality() {
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new DoubleTargetPlayerLifeEffect());
        addEffect(EffectSlot.SPELL, new ShuffleIntoLibraryEffect());
    }
}
