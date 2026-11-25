package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandCost;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentCastBySourceControllerThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "211")
public class CycleOfLife extends Card {

    public CycleOfLife() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new ReturnSelfToHandCost(),
                        new SetBasePowerToughnessEffect(0, 1, Subjects.target(),
                                Duration.UNTIL_CONTROLLERS_NEXT_UPKEEP),
                        RegisterDelayedStepTriggerEffect.aboutAtNext(Triggers.upkeep(ActivePlayer.YOU),
                                Subjects.target(),
                                PutCountersEffect.onTriggeringPermanent(
                                        CounterType.PLUS_ONE_PLUS_ONE, 1))),
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
