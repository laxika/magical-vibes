package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.ColorsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ECL", collectorNumber = "64")
public class RimeChill extends Card {

    public RimeChill() {
        // This spell costs {1} less to cast for each color among permanents you control.
        addStaticEffect(new ReduceOwnCastCostEffect(new ColorsAmongControlledPermanents()));

        // Tap up to two target creatures. Put a stun counter on each of them.
        target(TargetFilters.creature(), 0, 2)
                .addSpellEffect(new TapPermanentsEffect(TapUntapScope.TARGET))
                .addSpellEffect(new PutCounterOnTargetPermanentEffect(CounterType.STUN));

        // Draw a card.
        addSpellEffect(new DrawCardEffect(1));
    }
}
