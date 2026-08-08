package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnMultiplePermanentsToHandCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "BOK", collectorNumber = "52")
public class SoratamiMindsweeper extends Card {

    public SoratamiMindsweeper() {
        // {2}, Return a land you control to its owner's hand: Target player mills two cards.
        addActivatedAbility(new ActivatedAbility(false, "{2}",
                List.of(new ReturnMultiplePermanentsToHandCost(1, new PermanentIsLandPredicate()),
                        new MillEffect(2, MillRecipient.TARGET_PLAYER)),
                "{2}, Return a land you control to its owner's hand: Target player mills two cards."));
    }
}
