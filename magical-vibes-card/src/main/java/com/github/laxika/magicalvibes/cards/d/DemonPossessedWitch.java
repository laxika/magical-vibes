package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

public class DemonPossessedWitch extends Card {

    public DemonPossessedWitch() {
        target(TargetFilters.creature());
        addEffect(EffectSlot.ON_TRANSFORM_TO_BACK_FACE,
                new MayEffect(new DestroyTargetPermanentEffect(), "Destroy target creature?"));
    }
}
