package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AddManaForEachOtherLandWithSameNameEffect;

public class SasayasEssence extends Card {

    public SasayasEssence() {
        addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND,
                new AddManaForEachOtherLandWithSameNameEffect());
    }
}
