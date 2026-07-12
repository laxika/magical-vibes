package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "9ED", collectorNumber = "108")
@CardRegistration(set = "8ED", collectorNumber = "109")
public class TradeRoutes extends Card {

    public TradeRoutes() {
        // {1}: Return target land you control to its owner's hand.
        addActivatedAbility(new ActivatedAbility(
            false,
            "{1}",
            List.of(ReturnToHandEffect.target()),
            "{1}: Return target land you control to its owner's hand.",
            new ControlledPermanentPredicateTargetFilter(
                    new PermanentIsLandPredicate(),
                    "Target must be a land you control"
            )
        ));

        // {1}, Discard a land card: Draw a card.
        addActivatedAbility(new ActivatedAbility(
            false,
            "{1}",
            List.of(new DiscardCardTypeCost(new CardTypePredicate(CardType.LAND), "land"), new DrawCardEffect(1)),
            "{1}, Discard a land card: Draw a card."
        ));
    }
}
