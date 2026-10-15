package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerHasMoreCardsInHandThanControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "EXO", collectorNumber = "36")
public class KeeperOfTheMind extends Card {

    public KeeperOfTheMind() {
        // Choose target opponent who has at least two more cards in hand than you do as you
        // activate this ability. Draw a card.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}",
                List.of(new DrawCardEffect(1)),
                "{U}, {T}: Choose target opponent who has at least two more cards in hand than you do "
                        + "as you activate this ability. Draw a card.",
                new PlayerPredicateTargetFilter(
                        new PlayerHasMoreCardsInHandThanControllerPredicate(2),
                        "Target opponent must have at least two more cards in hand than you"
                )
        ));
    }
}
