package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.BecomeSaddledUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutUpToCardsFromHandOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SaddleCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentThatSaddledSourceThisTurnPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "206")
public class TheGitrogRavenousRide extends Card {

    public TheGitrogRavenousRide() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new MayEffect(SequenceEffect.of(
                        new SacrificePermanentsEffect(1, new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentThatSaddledSourceThisTurnPredicate())), Subjects.you()),
                                new ConditionalEffect(HappenedThisWay.sacrificed(), SequenceEffect.of(
                                new DrawCardEffect(AmountThisWay.sacrificedPower()),
                                new PutUpToCardsFromHandOntoBattlefieldEffect(
                                        new CardTypePredicate(CardType.LAND), "land",
                                        AmountThisWay.sacrificedPower(), true)))),
                        "Sacrifice a creature that saddled it this turn?"));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SaddleCost(1), new BecomeSaddledUntilEndOfTurnEffect(Subjects.source())),
                "Saddle 1",
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
