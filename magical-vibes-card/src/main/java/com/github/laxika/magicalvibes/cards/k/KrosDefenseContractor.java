package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GoadTriggeringCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "NCC", collectorNumber = "7")
@CardRegistration(set = "NCC", collectorNumber = "105")
public class KrosDefenseContractor extends Card {

    public KrosDefenseContractor() {
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.UPKEEP_TRIGGERED,
                        new PutCounterOnTargetPermanentEffect(CounterType.SHIELD, 1));

        addEffect(EffectSlot.ON_YOU_PUT_COUNTERS_ON_CREATURE_YOU_DONT_CONTROL,
                new TapPermanentsEffect(TapUntapScope.TRIGGERING));
        addEffect(EffectSlot.ON_YOU_PUT_COUNTERS_ON_CREATURE_YOU_DONT_CONTROL,
                new GoadTriggeringCreatureUntilNextTurnEffect());
        addEffect(EffectSlot.ON_YOU_PUT_COUNTERS_ON_CREATURE_YOU_DONT_CONTROL,
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TRIGGERING_PERMANENT,
                        GrantDuration.UNTIL_YOUR_NEXT_TURN));
    }
}
