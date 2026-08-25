package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.condition.CardDiscardedThisTurn;

import java.util.List;

@CardRegistration(set = "SPM", collectorNumber = "69")
public class SwarmBeingOfBees extends Card {

    public SwarmBeingOfBees() {
        addCastingOption(new GraveyardCast(null, "{B}", List.of(), new CardDiscardedThisTurn()));
    }
}
