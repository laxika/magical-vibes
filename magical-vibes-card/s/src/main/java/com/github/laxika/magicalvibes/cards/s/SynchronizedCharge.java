package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.HarmonizeCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "TDM", collectorNumber = "162")
public class SynchronizedCharge extends Card {

    public SynchronizedCharge() {
        target(TargetFilters.creatureYouControl(), 1, 2)
                .addEffect(EffectSlot.SPELL,
                        DistributeCountersAmongTargetsEffect.evenlyAmongTargets(
                                CounterType.PLUS_ONE_PLUS_ONE, 2));
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(
                Set.of(Keyword.VIGILANCE, Keyword.TRAMPLE),
                GrantScope.OWN_CREATURES,
                new PermanentHasCountersPredicate(CounterType.ANY)));
        addCastingOption(new HarmonizeCast("{4}{G}"));
    }
}
