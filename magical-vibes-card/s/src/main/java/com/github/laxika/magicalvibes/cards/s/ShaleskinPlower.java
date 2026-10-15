package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "LGN", collectorNumber = "110")
public class ShaleskinPlower extends Card {

    public ShaleskinPlower() {
        addMorph("{4}{R}");
        target(TargetFilters.land())
                .addEffect(EffectSlot.ON_TURNED_FACE_UP, new DestroyTargetPermanentEffect());
    }
}
