package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MKM", collectorNumber = "26")
public class NeighborhoodGuardian extends Card {

    public NeighborhoodGuardian() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                        new TriggeringPermanentConditionalEffect(
                                new PermanentPowerAtMostPredicate(2),
                                new BoostTargetCreatureEffect(1, 1)));
    }
}
