package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExileNCardsFromGraveyardCastingCost;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "40K", collectorNumber = "125")
public class Helbrute extends Card {

    public Helbrute() {
        addCastingOption(new GraveyardCast(List.of(
                new ExileNCardsFromGraveyardCastingCost(
                        new CardTypePredicate(CardType.CREATURE), "creature card", 1))));
    }
}
