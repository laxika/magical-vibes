package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.LifeLostThisTurn;
import com.github.laxika.magicalvibes.model.condition.OpponentLostLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "RTR", collectorNumber = "187")
public class RakdosLordOfRiots extends Card {

    public RakdosLordOfRiots() {
        // You can't cast this unless an opponent lost life this turn
        setCastCondition(new OpponentLostLifeThisTurn(1));
        // Flying, trample — loaded from Scryfall
        // Creature spells you cast cost {1} less for each 1 life your opponents have lost this turn
        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                new CardTypePredicate(CardType.CREATURE),
                new LifeLostThisTurn(CountScope.OPPONENTS),
                CostModificationScope.SELF));
    }
}
