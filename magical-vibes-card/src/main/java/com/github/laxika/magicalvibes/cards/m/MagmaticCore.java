package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DivisionMode;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "CSP", collectorNumber = "91")
public class MagmaticCore extends Card {

    public MagmaticCore() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CumulativeUpkeepEffect("{1}"));

        target(TargetFilters.creature(), 0, 99).addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new DealDividedDamageEffect(
                        new CountersOnSource(CounterType.AGE), null, DivisionMode.CHOSEN,
                        new PermanentIsCreaturePredicate(), 0, false, false, true));
    }
}
