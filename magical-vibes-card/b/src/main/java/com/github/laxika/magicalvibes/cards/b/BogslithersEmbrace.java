package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnControlledCreatureOrPayManaCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ECL", collectorNumber = "94")
public class BogslithersEmbrace extends Card {

    public BogslithersEmbrace() {
        addCastingCost(new PutCountersOnControlledCreatureOrPayManaCost(
                        CounterType.MINUS_ONE_MINUS_ONE, 1, "{3}"));
        target(TargetFilters.creature())
                .addSpellEffect(new ExileTargetPermanentEffect());
    }
}
