package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureThenManifestEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FRF", collectorNumber = "46")
public class RealityShift extends Card {

    public RealityShift() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new ExileTargetCreatureThenManifestEffect());
    }
}
