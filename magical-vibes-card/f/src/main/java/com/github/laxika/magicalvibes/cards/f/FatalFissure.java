package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EarthbendTargetLandEffect;
import com.github.laxika.magicalvibes.model.effect.ResolveEffectOnTargetDeathThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TLA", collectorNumber = "97")
public class FatalFissure extends Card {

    public FatalFissure() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new ResolveEffectOnTargetDeathThisTurnEffect(
                        new EarthbendTargetLandEffect(4)));
    }
}
