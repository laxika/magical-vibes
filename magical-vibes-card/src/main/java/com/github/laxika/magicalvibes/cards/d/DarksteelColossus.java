package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryReplacementEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M10", collectorNumber = "208")
public class DarksteelColossus extends Card {

    public DarksteelColossus() {
        addEffect(EffectSlot.STATIC, new ShuffleIntoLibraryReplacementEffect());
    }
}
