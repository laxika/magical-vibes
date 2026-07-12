package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "POR", collectorNumber = "41")
@CardRegistration(set = "PTK", collectorNumber = "34")
@CardRegistration(set = "8ED", collectorNumber = "62")
public class BalanceOfPower extends Card {

    public BalanceOfPower() {
        // "If target opponent has more cards in hand than you, draw cards equal to the difference."
        // Draw (target opponent's hand) - (your hand); a non-positive difference draws nothing.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addEffect(EffectSlot.SPELL, new DrawCardEffect(new Sum(
                new CardsInHand(CountScope.TARGET_PLAYER),
                new Scaled(new CardsInHand(CountScope.CONTROLLER), -1)
        )));
    }
}
