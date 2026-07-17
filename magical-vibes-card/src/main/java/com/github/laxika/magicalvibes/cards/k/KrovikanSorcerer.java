package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "96")
public class KrovikanSorcerer extends Card {

    public KrovikanSorcerer() {
        // {T}, Discard a nonblack card: Draw a card.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new DiscardCardTypeCost(new CardNotPredicate(new CardColorPredicate(CardColor.BLACK)), "nonblack"),
                        new DrawCardEffect(1)
                ),
                "{T}, Discard a nonblack card: Draw a card."
        ));

        // {T}, Discard a black card: Draw two cards, then discard one of them.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new DiscardCardTypeCost(new CardColorPredicate(CardColor.BLACK), "black"),
                        new DrawCardEffect(2),
                        new DiscardEffect(1, DiscardRecipient.CONTROLLER)
                ),
                "{T}, Discard a black card: Draw two cards, then discard one of them."
        ));
    }
}
