package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.condition.CardDiscardedThisTurn;

import java.util.List;

@CardRegistration(set = "SPM", collectorNumber = "91")
public class SpiderIslanders extends Card {

    public SpiderIslanders() {
        addCastingOption(new GraveyardCast(null, "{1}{R}", List.of(), new CardDiscardedThisTurn()));
    }
}
