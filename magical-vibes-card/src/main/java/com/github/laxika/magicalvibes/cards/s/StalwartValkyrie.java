package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExileCardFromGraveyardCastingCost;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "31")
public class StalwartValkyrie extends Card {

    public StalwartValkyrie() {
        addCastingOption(new AlternateHandCast(List.of(
                new ManaCastingCost("{1}{W}"),
                new ExileCardFromGraveyardCastingCost(new CardTypePredicate(CardType.CREATURE), "creature")
        )));
    }
}
