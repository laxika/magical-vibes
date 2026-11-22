package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.BecomeMonarchEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromOpponentSourcesEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "105")
@CardRegistration(set = "MSC", collectorNumber = "435")
public class HeartShapedHerb extends Card {

    public HeartShapedHerb() {
        addStaticEffect(new PreventDamageFromOpponentSourcesEffect(1));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new SacrificeSelfCost(),
                        new MayEffect(
                                SequenceEffect.of(
                                        new SacrificePermanentsEffect(new Fixed(1), new PermanentIsCreaturePredicate(), Subjects.you()),
                                        new ConditionalEffect(HappenedThisWay.sacrificed(), SequenceEffect.of(
                                                new PutCardsOntoBattlefieldEffect(Subjects.cardsThisWay(StepKind.SACRIFICE))
                                                        .withCounters(CounterType.PLUS_ONE_PLUS_ONE, 3),
                                                new BecomeMonarchEffect()))),
                                "Sacrifice a creature?")),
                "{2}, {T}, Sacrifice this artifact: You may sacrifice a creature. If you do, return that card to the battlefield under its owner's control with three +1/+1 counters on it and you become the monarch."
        ));
    }
}
