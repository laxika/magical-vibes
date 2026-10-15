package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "LGN", collectorNumber = "59")
public class AphettoExterminator extends Card {

    public AphettoExterminator() {
        addMorph("{3}{B}");
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_TURNED_FACE_UP, new BoostTargetCreatureEffect(-3, -3));
    }
}
