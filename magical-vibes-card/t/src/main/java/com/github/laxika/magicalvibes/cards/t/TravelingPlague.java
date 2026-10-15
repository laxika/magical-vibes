package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceAuraToChosenCreatureOnLeaveEffect;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ODY", collectorNumber = "166")
public class TravelingPlague extends Card {

    public TravelingPlague() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new PutCountersOnSelfEffect(CounterType.PLAGUE))
                .addEffect(EffectSlot.STATIC, new AttachedBoostEffect(
                        new Scaled(new CountersOnSource(CounterType.PLAGUE), -1),
                        new Scaled(new CountersOnSource(CounterType.PLAGUE), -1),
                        GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD,
                        new ReturnSourceAuraToChosenCreatureOnLeaveEffect());
    }
}
