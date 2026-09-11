package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MSH", collectorNumber = "163")
public class ClaimTheKingdom extends Card {

    public ClaimTheKingdom() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD, SequenceEffect.of(
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        new PutCountersOnSelfEffect(CounterType.PLAN)));

        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.ON_SELF_COUNTERS_PUT, new ConditionalEffect(
                        new SourceCounterThreshold(4, CounterType.PLAN),
                        SacrificeSelfThenEffect.reflexive(
                                new PutCounterOnTargetPermanentEffect(CounterType.INDESTRUCTIBLE))));
    }
}
