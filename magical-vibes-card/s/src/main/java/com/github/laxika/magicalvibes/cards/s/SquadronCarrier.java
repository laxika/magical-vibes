package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.ChosenPermanentPower;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TapCreatureCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YEOE", collectorNumber = "2")
public class SquadronCarrier extends Card {

    public SquadronCarrier() {
        ActivatedAbility conjurePilot = new ActivatedAbility(
                false,
                "{W}",
                List.of(new ConjureCardOntoBattlefieldEffect(StarfighterPilot::new, Set.of(), true)),
                "Exhaust — {W}: Conjure a card named Starfighter Pilot onto the battlefield. "
                        + "(Activate each exhaust ability only once.)"
        ).withMaxActivationsPerGame(1).withExhaust();
        addActivatedAbility(conjurePilot);

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new TapCreatureCost(new PermanentIsCreaturePredicate(), true, true, false, true),
                        new PutCountersOnSelfEffect(CounterType.CHARGE, new ChosenPermanentPower())
                ),
                "Station — Tap another creature you control: Put charge counters equal to its power "
                        + "on this Spacecraft.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));

        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                conjurePilot,
                GrantScope.OWN_PERMANENTS,
                new PermanentHasSubtypePredicate(CardSubtype.SPACECRAFT)));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceCounterThreshold(10, CounterType.CHARGE),
                new AnimatePermanentsEffect(
                        (DynamicAmount) null, (DynamicAmount) null, List.of(), Set.of(), null,
                        Set.of(CardType.CREATURE), GrantScope.SELF, EffectDuration.CONTINUOUS, null
                )));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceCounterThreshold(10, CounterType.CHARGE),
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.ALL_OWN_CREATURES)));
    }
}
