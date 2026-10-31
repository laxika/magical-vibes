package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsDrawnThisTurn;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;

public class HeliodTheWarpedEclipse extends Card {

    public HeliodTheWarpedEclipse() {
        addStaticEffect(new GrantFlashToCardTypeEffect(null));
        addStaticEffect(new ReduceCastCostForMatchingSpellsEffect(
                new CardTruePredicate(),
                new CardsDrawnThisTurn(CountScope.OPPONENTS),
                CostModificationScope.SELF
        ));
    }
}
