package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsDrawnThisTurn;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MSC", collectorNumber = "690")
public class InspiredFire extends Card {

    public InspiredFire() {
        addStaticEffect(new ReduceOwnCastCostEffect(new CardsDrawnThisTurn()));
        target(TargetFilters.creature())
                .addSpellEffect(new DealDamageToTargetCreatureEffect(4));
    }
}
