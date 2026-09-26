package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

/** Maestro's Gift, the prepared spell of Inspired Skypainter (SOC 48). */
public class MaestrosGift extends Card {

    public MaestrosGift() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.SPELL, new CreateTokenCopyOfTargetPermanentEffect(true, false));
    }
}
