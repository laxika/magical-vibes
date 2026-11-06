package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AddOnePlusOneCountersToModularAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetForEachDyingSourceCounterEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "243")
public class ZabazTheGlimmerwasp extends Card {

    public ZabazTheGlimmerwasp() {
        addStaticEffect(new AddOnePlusOneCountersToModularAbilityEffect());
        addStaticEffect(new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(1)));
        addTrigger(Triggers.dies(ObjectRelation.THIS),
                new MayEffect(PutCounterOnTargetForEachDyingSourceCounterEffect.modularAbility(),
                        PutCounterOnTargetForEachDyingSourceCounterEffect.MODULAR_PROMPT));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new DestroyTargetPermanentEffect()),
                "{R}: Destroy target artifact you control.",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentIsArtifactPredicate(), "Target must be an artifact you control")));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)),
                "{W}: Zabaz gains flying until end of turn."));
    }
}
