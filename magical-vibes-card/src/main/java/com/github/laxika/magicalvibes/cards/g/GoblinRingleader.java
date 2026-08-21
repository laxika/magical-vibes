package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsMatchingToHandRestToBottomEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "143")
@CardRegistration(set = "APC", collectorNumber = "62")
public class GoblinRingleader extends Card {

    public GoblinRingleader() {
        // When Goblin Ringleader enters, reveal the top four cards of your library. Put all Goblin
        // cards revealed this way into your hand and the rest on the bottom of your library in any order.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new RevealTopCardsMatchingToHandRestToBottomEffect(
                4,
                new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.GOBLIN),
                        new CardKeywordPredicate(Keyword.CHANGELING)))));
    }
}
