package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ClassLevelUpEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.IncreaseSpellCostEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "29")
public class PaladinClass extends Card {

    public PaladinClass() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControllerTurn(),
                new IncreaseSpellCostEffect(new CardTruePredicate(), 1, CostModificationScope.OPPONENT)));

        addActivatedAbility(new ActivatedAbility(
                false, "{2}{W}", List.of(new ClassLevelUpEffect(2)),
                "Gain the next level as a sorcery.", ActivationTimingRestriction.SORCERY_SPEED
        ).withActivationCondition(
                new NotCondition(new SourceCounterThreshold(1, CounterType.LEVEL)),
                "this Class is level 1"));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceCounterThreshold(1, CounterType.LEVEL),
                new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES)));

        DynamicAmount otherAttackingCreatures = new Sum(
                new PermanentCount(new PermanentIsAttackingPredicate(), CountScope.CONTROLLER),
                new Fixed(-1));
        target(TargetFilters.attackingCreature())
                .addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, new ConditionalEffect(
                        new SourceCounterThreshold(2, CounterType.LEVEL),
                        new BoostTargetCreatureEffect(otherAttackingCreatures, otherAttackingCreatures)))
                .addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, new ConditionalEffect(
                        new SourceCounterThreshold(2, CounterType.LEVEL),
                        new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.TARGET)));

        addActivatedAbility(new ActivatedAbility(
                false, "{4}{W}", List.of(new ClassLevelUpEffect(3)),
                "Gain the next level as a sorcery.", ActivationTimingRestriction.SORCERY_SPEED
        ).withActivationCondition(new AllOf(List.of(
                new SourceCounterThreshold(1, CounterType.LEVEL),
                new NotCondition(new SourceCounterThreshold(2, CounterType.LEVEL)))),
                "this Class is level 2"));
    }
}
