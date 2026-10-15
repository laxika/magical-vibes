package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnMultiplePermanentsToHandCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "90")
public class SoratamiSavant extends Card {

    public SoratamiSavant() {
        // {3}, Return a land you control to its owner's hand: Counter target spell unless its
        // controller pays {3}.
        addActivatedAbility(new ActivatedAbility(false, "{3}",
                List.of(new ReturnMultiplePermanentsToHandCost(1, new PermanentIsLandPredicate()),
                        new CounterUnlessPaysEffect(3)),
                "{3}, Return a land you control to its owner's hand: Counter target spell unless its controller pays {3}."));
    }
}
