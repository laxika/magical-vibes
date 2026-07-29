package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandCost;
import com.github.laxika.magicalvibes.model.effect.SetTargetBasePowerToughnessUntilNextUpkeepThenAddCounterEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentCastBySourceControllerThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "211")
public class CycleOfLife extends Card {

    public CycleOfLife() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new ReturnSelfToHandCost(),
                        new SetTargetBasePowerToughnessUntilNextUpkeepThenAddCounterEffect(0, 1)),
                "Return Cycle of Life to its owner's hand: Target creature you cast this turn has base "
                        + "power and toughness 0/1 until your next upkeep. At the beginning of your next "
                        + "upkeep, put a +1/+1 counter on that creature.",
                new PermanentPredicateTargetFilter(new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentCastBySourceControllerThisTurnPredicate())),
                        "Target must be a creature you cast this turn.")
        ));
    }
}
