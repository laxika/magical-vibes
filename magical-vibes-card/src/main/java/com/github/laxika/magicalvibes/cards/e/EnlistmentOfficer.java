package com.github.laxika.magicalvibes.cards.e;

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

@CardRegistration(set = "APC", collectorNumber = "9")
public class EnlistmentOfficer extends Card {

    public EnlistmentOfficer() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new RevealTopCardsMatchingToHandRestToBottomEffect(
                4,
                new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.SOLDIER),
                        new CardKeywordPredicate(Keyword.CHANGELING)))));
    }
}
