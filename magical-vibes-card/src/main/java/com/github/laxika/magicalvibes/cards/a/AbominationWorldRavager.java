package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.condition.CardDiscardedThisTurn;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "47")
@CardRegistration(set = "MSC", collectorNumber = "355")
public class AbominationWorldRavager extends Card {

    public AbominationWorldRavager() {
        addCastingOption(new GraveyardCast(null, "{4}{R}", List.of(), new CardDiscardedThisTurn()));
    }
}
