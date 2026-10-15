package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

public class RescueTheFoal extends Card {

    public RescueTheFoal() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.SPELL, FlickerEffect.flickerTarget());
    }
}
