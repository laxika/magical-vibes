package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ROE", collectorNumber = "64")
public class EchoMage extends Card {

    public EchoMage() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(new PutCountersOnSelfEffect(CounterType.LEVEL)),
                "Level up {1}{U} ({1}{U}: Put a level counter on this. Level up only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED
        ));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new AllOf(List.of(
                        new SourceCounterThreshold(2, CounterType.LEVEL),
                        new NotCondition(new SourceCounterThreshold(4, CounterType.LEVEL)))),
                new SetBasePowerToughnessEffect(2, 4, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceCounterThreshold(4, CounterType.LEVEL),
                new SetBasePowerToughnessEffect(2, 5, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new AllOf(List.of(
                        new SourceCounterThreshold(2, CounterType.LEVEL),
                        new NotCondition(new SourceCounterThreshold(4, CounterType.LEVEL)))),
                new GrantActivatedAbilityEffect(copyAbility(1), GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceCounterThreshold(4, CounterType.LEVEL),
                new GrantActivatedAbilityEffect(copyAbility(2), GrantScope.SELF)));
    }

    private static ActivatedAbility copyAbility(int copyCount) {
        List<CardEffect> copies = copyCount == 1
                ? List.of(new CopySpellEffect())
                : List.of(new CopySpellEffect(), new CopySpellEffect());
        return new ActivatedAbility(
                true,
                "{U}{U}",
                copies,
                copyCount == 1
                        ? "{U}{U}, {T}: Copy target instant or sorcery spell. You may choose new targets for the copy."
                        : "{U}{U}, {T}: Copy target instant or sorcery spell twice. You may choose new targets for the copies.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryTypeInPredicate(Set.of(StackEntryType.INSTANT_SPELL, StackEntryType.SORCERY_SPELL)),
                        "Target must be an instant or sorcery spell."
                )
        );
    }
}
