package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.condition.CardDiscardedThisTurn;

import java.util.List;

@CardRegistration(set = "SPM", collectorNumber = "85")
public class RagingGoblinoids extends Card {

    public RagingGoblinoids() {
        addCastingOption(new GraveyardCast(null, "{2}{R}", List.of(), new CardDiscardedThisTurn()));
    }
}
