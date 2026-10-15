package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsDrawnThisTurn;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;

public class HeliodTheWarpedEclipse extends Card {

    public HeliodTheWarpedEclipse() {
        addEffect(EffectSlot.STATIC, new GrantFlashToCardTypeEffect(null));
        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                new CardTruePredicate(),
                new CardsDrawnThisTurn(CountScope.OPPONENTS),
                CostModificationScope.SELF
        ));
    }
}
