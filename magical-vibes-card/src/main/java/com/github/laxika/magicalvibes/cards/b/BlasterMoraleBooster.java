package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MoveCountersFromSourceToTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetForEachDyingSourceCounterEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

public class BlasterMoraleBooster extends Card {

    public BlasterMoraleBooster() {
        PermanentPredicate artifactTarget = new PermanentAllOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentNotPredicate(
                        new PermanentIsSourcePermanentPredicate())));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(3)));
        addEffect(EffectSlot.ON_DEATH, new PutCounterOnTargetForEachDyingSourceCounterEffect(
                CounterType.PLUS_ONE_PLUS_ONE, true,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsCreaturePredicate()))));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}",
                List.of(
                        new MoveCountersFromSourceToTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET),
                        new ConditionalEffect(
                                new NotCondition(new SourceCounterThreshold(
                                        1, CounterType.PLUS_ONE_PLUS_ONE)),
                                new TransformSelfEffect())
                ),
                "{X}, {T}: Move X +1/+1 counters from Blaster onto another target artifact. That artifact gains haste until end of turn. If Blaster has no +1/+1 counters on it, convert it. Activate only as a sorcery.",
                new PermanentPredicateTargetFilter(artifactTarget, "Target must be another artifact"),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
