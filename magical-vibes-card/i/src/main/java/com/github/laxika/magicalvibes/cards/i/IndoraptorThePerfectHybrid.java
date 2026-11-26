package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePlayerAtRandomEffect;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DamageDealtToOpponentsThisTurn;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "REX", collectorNumber = "15")
@CardRegistration(set = "REX", collectorNumber = "40")
public class IndoraptorThePerfectHybrid extends Card {

    public IndoraptorThePerfectHybrid() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE,
                        new DamageDealtToOpponentsThisTurn()));
        // Enrage — Whenever Indoraptor is dealt damage, choose an opponent at random. Indoraptor deals damage
        // equal to its power to that player unless they sacrifice a nontoken creature of their choice.
        addTrigger(Triggers.dealtDamage(), SequenceEffect.of(
                new ChoosePlayerAtRandomEffect(Subjects.eachOpponent()),
                UnlessPaysEffect.unlessPays(Subjects.playersThisWay(StepKind.CHOOSE),
                        new SacrificePermanentCost(new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentIsTokenPredicate()))),
                                "nontoken creature", false),
                        DealDamageEffect.to(Subjects.playersThisWay(StepKind.CHOOSE), new SourcePower()))));
    }
}
