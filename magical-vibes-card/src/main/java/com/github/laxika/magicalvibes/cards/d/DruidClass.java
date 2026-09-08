package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ClassLevelUpEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PlaysAdditionalLandEachTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AFR", collectorNumber = "180")
public class DruidClass extends Card {

    public DruidClass() {
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD, new GainLifeEffect(1));

        addActivatedAbility(new ActivatedAbility(
                false, "{2}{G}", List.of(new ClassLevelUpEffect(2)),
                "Gain the next level as a sorcery.", ActivationTimingRestriction.SORCERY_SPEED
        ).withActivationCondition(
                new NotCondition(new SourceCounterThreshold(1, CounterType.LEVEL)),
                "this Class is level 1"));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceCounterThreshold(1, CounterType.LEVEL),
                new PlaysAdditionalLandEachTurnEffect(1)));

        PermanentCount landsYouControl = new PermanentCount(
                new PermanentIsLandPredicate(), CountScope.CONTROLLER);
        addActivatedAbility(new ActivatedAbility(
                false, "{4}{G}", List.of(new ClassLevelUpEffect(3, List.of(
                        new AnimatePermanentsEffect(
                                landsYouControl, landsYouControl, List.of(), Set.of(Keyword.HASTE), null,
                                Set.of(), GrantScope.TARGET, EffectDuration.PERMANENT,
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsLandPredicate(),
                                        new PermanentControlledBySourceControllerPredicate())))
                ))),
                "Gain the next level as a sorcery.", ActivationTimingRestriction.SORCERY_SPEED
        ).withActivationCondition(new AllOf(List.of(
                new SourceCounterThreshold(1, CounterType.LEVEL),
                new NotCondition(new SourceCounterThreshold(2, CounterType.LEVEL)))),
                "this Class is level 2"));
    }
}
