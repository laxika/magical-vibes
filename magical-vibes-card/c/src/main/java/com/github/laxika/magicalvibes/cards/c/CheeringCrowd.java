package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.TotalCountersOnSource;
import com.github.laxika.magicalvibes.model.effect.AwardManaToActivePlayerEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PutTypedCounterOnSourceCost;
import java.util.List;

@CardRegistration(set = "SPM", collectorNumber = "126")
@CardRegistration(set = "SPM", collectorNumber = "270")
@CardRegistration(set = "OM1", collectorNumber = "129")
public class CheeringCrowd extends Card {

    public CheeringCrowd() {
        addEffect(EffectSlot.EACH_PRECOMBAT_MAIN_TRIGGERED,
                new ForcedCostOrElseEffect(
                        new PutTypedCounterOnSourceCost(CounterType.PLUS_ONE_PLUS_ONE),
                        List.of(), true, false, true, false,
                        List.of(new AwardManaToActivePlayerEffect(
                                ManaColor.COLORLESS, new TotalCountersOnSource()))));
    }
}
