package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DivisionMode;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CSP", collectorNumber = "91")
public class MagmaticCore extends Card {

    public MagmaticCore() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new CumulativeUpkeepEffect("{1}"));

        targetUpTo(new CountersOnSource(CounterType.AGE), TargetFilters.creature(), Integer.MAX_VALUE).addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new DealDividedDamageEffect(
                        new CountersOnSource(CounterType.AGE), null, DivisionMode.CHOSEN,
                        new PermanentIsCreaturePredicate(), 0, false, false, true));
    }
}
