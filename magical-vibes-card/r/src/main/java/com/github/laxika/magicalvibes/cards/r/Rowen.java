package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealDrawnCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "7ED", collectorNumber = "266")
@CardRegistration(set = "6ED", collectorNumber = "250")
@CardRegistration(set = "VIS", collectorNumber = "119")
public class Rowen extends Card {

    public Rowen() {
        // Reveal the first card you draw each turn. Whenever you reveal a basic land card this way, draw a card.
        addStaticEffect(RevealDrawnCardsEffect.firstEachTurn());
        addTrigger(Triggers.youRevealCardThisWay().withFilter(new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.LAND), new CardSupertypePredicate(CardSupertype.BASIC)))),
                new DrawCardEffect(1));
    }
}
