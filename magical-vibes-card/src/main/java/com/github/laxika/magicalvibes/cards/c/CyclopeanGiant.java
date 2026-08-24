package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantBasicLandTypeToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TSP", collectorNumber = "100")
public class CyclopeanGiant extends Card {

    public CyclopeanGiant() {
        target(TargetFilters.land()).addEffect(EffectSlot.ON_DEATH, SequenceEffect.of(
                new GrantBasicLandTypeToTargetEffect(EffectDuration.CONTINUOUS, CardSubtype.SWAMP, true),
                new ExileSourceCardFromGraveyardEffect()));
    }
}
