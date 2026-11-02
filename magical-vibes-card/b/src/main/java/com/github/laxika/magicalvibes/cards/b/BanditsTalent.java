package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PlayersWithCardsInHandAtMost;
import com.github.laxika.magicalvibes.model.condition.ActivePlayerHandAtMost;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ClassLevelUpEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentDiscardsTwoUnlessNonlandEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "83")
public class BanditsTalent extends Card {

    public BanditsTalent() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new EachOpponentDiscardsTwoUnlessNonlandEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(new ClassLevelUpEffect(2)),
                "Level up {B} ({B}: Put a level counter on this. Level up only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withActivationCondition(
                new NotCondition(new SourceCounterThreshold(1, CounterType.LEVEL)),
                "This Class is already level 2 or higher."));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{B}",
                List.of(new ClassLevelUpEffect(3)),
                "Level up {3}{B} ({3}{B}: Put a level counter on this. Level up only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withActivationCondition(
                new AllOf(List.of(
                        new SourceCounterThreshold(1, CounterType.LEVEL),
                        new NotCondition(new SourceCounterThreshold(2, CounterType.LEVEL)))),
                "This Class must be level 2."));

        addTrigger(Triggers.upkeep(ActivePlayer.OPPONENT), new ConditionalEffect(
                new AllOf(List.of(new SourceCounterThreshold(1, CounterType.LEVEL),
                        new ActivePlayerHandAtMost(1))),
                new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PLAYER)));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new ConditionalEffect(
                new SourceCounterThreshold(2, CounterType.LEVEL),
                new DrawCardEffect(new PlayersWithCardsInHandAtMost(CountScope.OPPONENTS, 1))));
    }
}
