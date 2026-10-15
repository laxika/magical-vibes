package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PhaseOutEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutSubject;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedEndStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "CMM", collectorNumber = "729")
public class VronosMaskedInquisitor extends Card {

    public VronosMaskedInquisitor() {
        var otherPlaneswalkerYouControl = new ControlledPermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsPlaneswalkerPredicate(),
                        new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
                )),
                "Target must be another planeswalker you control"
        );
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new RegisterDelayedEndStepTriggerEffect(
                        List.of(), new PhaseOutEffect(PhaseOutSubject.TARGET))),
                "+1: Up to two other target planeswalkers you control phase out at the beginning of the next end step.",
                otherPlaneswalkerYouControl,
                +1,
                null,
                null,
                List.of(),
                0,
                2
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(ReturnToHandEffect.target()),
                "−2: For each opponent, return up to one target nonland permanent that player controls to its owner's hand.",
                TargetFilters.nonlandPermanentAnOpponentControls(),
                -2,
                null,
                null,
                List.of(),
                0,
                99
        ).withMultiTargetConstraint(MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER));

        var artifactYouControl = new ControlledPermanentPredicateTargetFilter(
                new PermanentIsArtifactPredicate(), "Target must be an artifact you control");
        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(
                        new AnimatePermanentsEffect(
                                9,
                                9,
                                List.of(CardSubtype.CONSTRUCT),
                                Set.of(Keyword.VIGILANCE, Keyword.INDESTRUCTIBLE),
                                null,
                                Set.of(CardType.CREATURE),
                                GrantScope.TARGET,
                                EffectDuration.PERMANENT
                        ),
                        new GrantEffectToTargetEffect(
                                EffectSlot.STATIC,
                                new CantBeBlockedEffect(),
                                EffectDuration.PERMANENT,
                                false
                        )
                ),
                "−7: Target artifact you control becomes a 9/9 Construct artifact creature and gains vigilance, indestructible, and \"This creature can't be blocked.\"",
                artifactYouControl
        ));
    }
}
