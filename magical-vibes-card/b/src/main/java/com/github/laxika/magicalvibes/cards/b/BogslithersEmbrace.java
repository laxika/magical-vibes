package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.AnyOfCost;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnControlledCreatureCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ECL", collectorNumber = "94")
public class BogslithersEmbrace extends Card {

    public BogslithersEmbrace() {
        addCastingCost(AnyOfCost.of(PutCounterOnControlledCreatureCost.blight(1), new PayManaCost("{3}")));
        target(TargetFilters.creature())
                .addSpellEffect(new ExileTargetPermanentEffect());
    }
}
