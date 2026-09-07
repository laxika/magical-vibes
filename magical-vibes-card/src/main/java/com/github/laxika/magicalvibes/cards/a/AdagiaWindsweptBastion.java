package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.ChosenPermanentPower;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TapCreatureCost;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EOE", collectorNumber = "250")
public class AdagiaWindsweptBastion extends Card {

    public AdagiaWindsweptBastion() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new TapCreatureCost(new PermanentIsCreaturePredicate(), true, true),
                        new PutCountersOnSelfEffect(CounterType.CHARGE, new ChosenPermanentPower())
                ),
                "Tap another creature you control: Put charge counters equal to its power on Adagia, "
                        + "Windswept Bastion.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}{W}",
                List.of(CreateTokenCopyOfTargetPermanentEffect.withAdditionalSupertypes(
                        Set.of(CardSupertype.LEGENDARY))),
                "{3}{W}, {T}: Create a token that's a copy of target artifact or enchantment you control, "
                        + "except it's legendary.",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsEnchantmentPredicate()
                        )),
                        "Target must be an artifact or enchantment you control"
                ),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ).withRequiredSourceCounters(CounterType.CHARGE, 12));
    }
}
