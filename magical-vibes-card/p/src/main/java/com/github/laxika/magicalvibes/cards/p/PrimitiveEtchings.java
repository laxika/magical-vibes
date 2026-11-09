package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealDrawnCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SCG", collectorNumber = "126")
public class PrimitiveEtchings extends Card {

    public PrimitiveEtchings() {
        // Reveal the first card you draw each turn. Whenever you reveal a creature card this way, draw a card.
        addStaticEffect(RevealDrawnCardsEffect.firstEachTurn());
        addTrigger(Triggers.youRevealCardThisWay().withFilter(new CardTypePredicate(CardType.CREATURE)),
                new DrawCardEffect(1));
    }
}
