package com.github.laxika.magicalvibes.cards.s;

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

@CardRegistration(set = "ORI", collectorNumber = "199")
@CardRegistration(set = "APC", collectorNumber = "87")
public class SylvanMessenger extends Card {

    public SylvanMessenger() {
        // When Sylvan Messenger enters, reveal the top four cards of your library. Put all Elf cards
        // revealed this way into your hand and the rest on the bottom of your library in any order.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new RevealTopCardsMatchingToHandRestToBottomEffect(
                4,
                new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.ELF),
                        new CardKeywordPredicate(Keyword.CHANGELING)))));
    }
}
