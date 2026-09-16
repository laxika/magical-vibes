package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControlledPermanentsEnterWithAdditionalCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.MoveCountersFromSourceToTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetForEachDyingSourceCounterEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "BOT", collectorNumber = "8")
@CardRegistration(set = "BOT", collectorNumber = "22")
public class BlasterCombatDJ extends Card {

    public BlasterCombatDJ() {
        setBackFaceCard(new BlasterMoraleBooster());
        addCastingOption(AlternateHandCast.moreThanMeetsTheEye("{1}{R}{G}"));

        PermanentPredicate artifactCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsCreaturePredicate()));
        PermanentPredicate modularEntryTarget = new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentIsTokenPredicate()),
                new PermanentIsArtifactPredicate(),
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasSubtypePredicate(CardSubtype.VEHICLE)))));
        PermanentPredicate modularTarget = new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentIsTokenPredicate()),
                new PermanentIsArtifactPredicate(),
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasSubtypePredicate(CardSubtype.VEHICLE))),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())));

        addEffect(EffectSlot.STATIC,
                new ControlledPermanentsEnterWithAdditionalCountersEffect(modularEntryTarget, 1));
        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.ON_DEATH,
                new PutCounterOnTargetForEachDyingSourceCounterEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, true, artifactCreature),
                GrantScope.OWN_PERMANENTS,
                modularTarget));
        addEffect(EffectSlot.ON_SELF_PLUS_ONE_PLUS_ONE_COUNTERS_PUT, new TransformSelfEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "BlasterMoraleBooster";
    }
}
