package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ClassLevelUpEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BLC", collectorNumber = "31")
public class GourmandsTalent extends Card {

    private static final PermanentIsArtifactPredicate ARTIFACT = new PermanentIsArtifactPredicate();

    public GourmandsTalent() {
        ActivatedAbility foodAbility = new ActivatedAbility(
                true,
                "{2}",
                List.of(new SacrificeSelfCost(), new GainLifeEffect(3)),
                "{2}, {T}, Sacrifice this artifact: You gain 3 life."
        );
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControllerTurn(),
                new GrantCardTypeEffect(CardType.ARTIFACT, GrantScope.OWN_PERMANENTS, ARTIFACT)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControllerTurn(),
                new GrantSubtypeEffect(CardSubtype.FOOD, GrantScope.OWN_PERMANENTS, false, ARTIFACT)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControllerTurn(),
                new GrantActivatedAbilityEffect(foodAbility, GrantScope.OWN_PERMANENTS, ARTIFACT)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}",
                List.of(new ClassLevelUpEffect(2)),
                "Gain the next level as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withActivationCondition(
                new NotCondition(new SourceCounterThreshold(1, CounterType.LEVEL)),
                "this Class is level 1"));

        addEffect(EffectSlot.ON_CONTROLLER_GAINS_LIFE, new ConditionalEffect(
                new SourceCounterThreshold(1, CounterType.LEVEL),
                new OncePerTurnTriggerEffect(new CreateTokenEffect(
                        1,
                        "Raccoon",
                        3,
                        3,
                        CardColor.GREEN,
                        List.of(CardSubtype.RACCOON),
                        Set.of(),
                        Set.of()))));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{G}",
                List.of(new ClassLevelUpEffect(3)),
                "Gain the next level as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withActivationCondition(
                new AllOf(List.of(
                        new SourceCounterThreshold(1, CounterType.LEVEL),
                        new NotCondition(new SourceCounterThreshold(2, CounterType.LEVEL)))),
                "this Class is level 2"));

        addEffect(EffectSlot.ON_CONTROLLER_GAINS_LIFE, new ConditionalEffect(
                new SourceCounterThreshold(2, CounterType.LEVEL),
                new OncePerTurnTriggerEffect(new PutCounterOnEachControlledPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE,
                        1,
                        new PermanentIsCreaturePredicate()))));
    }
}
