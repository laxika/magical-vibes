package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.condition.SourceClassLevelAtLeast;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ClassLevelUpEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TMT", collectorNumber = "34")
public class DoesMachines extends Card {

    public DoesMachines() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MillEffect(2, MillRecipient.CONTROLLER));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(2));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DiscardEffect(2, DiscardRecipient.CONTROLLER));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(new ClassLevelUpEffect(2)),
                "{1}{U}: Gain the next level as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withActivationCondition(
                new NotCondition(new SourceCounterThreshold(1, CounterType.LEVEL)),
                "This Class is already level 2 or higher."));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{U}",
                List.of(new ClassLevelUpEffect(3)),
                "{4}{U}: Gain the next level as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withActivationCondition(
                new AllOf(List.of(
                        new SourceCounterThreshold(1, CounterType.LEVEL),
                        new NotCondition(new SourceCounterThreshold(2, CounterType.LEVEL)))),
                "This Class must be level 2."));

        // When this Class becomes level 2, return up to two target artifact cards from your graveyard to your hand.
        addEffect(EffectSlot.ON_SELF_REACHES_LEVEL_TWO, ReturnTargetCardsFromGraveyardToHandEffect.forTriggeredAbility(
                new CardTypePredicate(CardType.ARTIFACT), 2));

        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentIsArtifactPredicate(),
                "Target must be an artifact you control"))
                .addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                        new ConditionalEffect(new SourceClassLevelAtLeast(3),
                                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 3)))
                .addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                        new ConditionalEffect(
                                new AllOf(List.of(new SourceClassLevelAtLeast(3),
                                        new NotCondition(new TargetPermanentMatches(new PermanentIsCreaturePredicate())))),
                                new AnimatePermanentsEffect(
                                        0,
                                        0,
                                        List.of(CardSubtype.ROBOT),
                                        Set.of(),
                                        null,
                                        Set.of(CardType.CREATURE),
                                        GrantScope.TARGET,
                                        EffectDuration.PERMANENT)));
    }
}
