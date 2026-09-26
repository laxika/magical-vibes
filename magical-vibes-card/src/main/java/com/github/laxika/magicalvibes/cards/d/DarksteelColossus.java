package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryReplacementEffect;

@CardRegistration(set = "M10", collectorNumber = "208")
@CardRegistration(set = "DST", collectorNumber = "109")
@CardRegistration(set = "SLD", collectorNumber = "57")
@CardRegistration(set = "SLD", collectorNumber = "1081")
public class DarksteelColossus extends Card {

    public DarksteelColossus() {
        addEffect(EffectSlot.STATIC, new ShuffleIntoLibraryReplacementEffect());
    }
}
