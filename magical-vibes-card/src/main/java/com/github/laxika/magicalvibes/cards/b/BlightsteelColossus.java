package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryReplacementEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "MBS", collectorNumber = "99")
public class BlightsteelColossus extends Card {

    public BlightsteelColossus() {
        addEffect(EffectSlot.STATIC, new ShuffleIntoLibraryReplacementEffect());
    }
}
