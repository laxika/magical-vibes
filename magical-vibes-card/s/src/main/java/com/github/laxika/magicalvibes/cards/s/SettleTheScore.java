package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DOM", collectorNumber = "103")
public class SettleTheScore extends Card {

    public SettleTheScore() {
        // Exile target creature. Put two loyalty counters on a planeswalker you control.
        target(TargetFilters.creature())
                .addSpellEffect(new ExileTargetPermanentEffect())
                .addSpellEffect(new PutCounterOnTargetPermanentEffect(
                        CounterType.LOYALTY, 2, new PermanentIsPlaneswalkerPredicate()));
    }
}
