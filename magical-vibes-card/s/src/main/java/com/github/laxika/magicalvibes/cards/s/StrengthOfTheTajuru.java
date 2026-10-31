package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "WWK", collectorNumber = "113")
@CardRegistration(set = "C20", collectorNumber = "192")
public class StrengthOfTheTajuru extends Card {

    public StrengthOfTheTajuru() {
        setAdditionalCostPerExtraTarget(1);
        addCastingCost(RepeatableAdditionalManaCost.multikicker(List.of("{1}")));
        target(TargetFilters.creature(), 1, Integer.MAX_VALUE)
                .addSpellEffect(new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue()));
    }
}
