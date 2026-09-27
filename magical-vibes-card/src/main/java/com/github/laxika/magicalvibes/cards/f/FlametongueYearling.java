package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.RepeatedAdditionalCostCount;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.effect.SourcePermanentDealsPowerDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "125")
public class FlametongueYearling extends Card {

    public FlametongueYearling() {
        addEffect(EffectSlot.SPELL, RepeatableAdditionalManaCost.multikicker(List.of("{2}")));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithCountersEffect(
                CounterType.PLUS_ONE_PLUS_ONE, new RepeatedAdditionalCostCount("{2}")));
        target(TargetFilters.creature()).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new SourcePermanentDealsPowerDamageToTargetCreatureEffect());
    }
}
