package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandThenDrawThatManyEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnMultiplePermanentsToHandCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "91")
public class SoratamiSeer extends Card {

    public SoratamiSeer() {
        // {4}, Return two lands you control to their owner's hand: Discard all the cards in your
        // hand, then draw that many cards.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}",
                List.of(new ReturnMultiplePermanentsToHandCost(2, new PermanentIsLandPredicate()),
                        new DiscardOwnHandThenDrawThatManyEffect()),
                "{4}, Return two lands you control to their owner's hand: Discard all the cards in your hand, then draw that many cards."
        ));
    }
}
