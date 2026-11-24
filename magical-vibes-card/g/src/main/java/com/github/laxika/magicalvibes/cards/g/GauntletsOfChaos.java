package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfTargetPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "373")
@CardRegistration(set = "CHR", collectorNumber = "99")
@CardRegistration(set = "LEG", collectorNumber = "278")
@CardRegistration(set = "ME3", collectorNumber = "196")
public class GauntletsOfChaos extends Card {

    private static final PermanentPredicate ARTIFACT_CREATURE_OR_LAND = new PermanentAnyOfPredicate(List.of(
            new PermanentIsArtifactPredicate(),
            new PermanentIsCreaturePredicate(),
            new PermanentIsLandPredicate()));

    public GauntletsOfChaos() {
        // {5}, Sacrifice this artifact: Exchange control of target artifact, creature, or land you
        // control and target permanent an opponent controls that shares one of those types with it.
        // If those permanents are exchanged this way, destroy all Auras attached to them.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}",
                List.of(new SacrificeSelfCost(),
                        ExchangeControlOfTargetPermanentsEffect.forControlledTargetsSharingOneOf(
                                ARTIFACT_CREATURE_OR_LAND,
                                ExchangeControlOfTargetPermanentsEffect.ARTIFACT_CREATURE_OR_LAND),
                        new ConditionalEffect(new HappenedThisWay(StepKind.GAIN_CONTROL),
                                new DestroyTargetPermanentEffect(Subjects.attachmentsOf(
                                        Subjects.thisWay(StepKind.GAIN_CONTROL),
                                        new PermanentHasSubtypePredicate(CardSubtype.AURA))))),
                "{5}, Sacrifice this artifact: Exchange control of target artifact, creature, or land you "
                        + "control and target permanent an opponent controls that shares one of those types "
                        + "with it. If those permanents are exchanged this way, destroy all Auras attached to them.",
                List.of(
                        new ControlledPermanentPredicateTargetFilter(
                                ARTIFACT_CREATURE_OR_LAND,
                                "Target must be an artifact, creature, or land you control"),
                        new PermanentPredicateTargetFilter(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
                                        ARTIFACT_CREATURE_OR_LAND)),
                                "Target must be an artifact, creature, or land an opponent controls")
                ),
                2,
                2
        ).withMultiTargetConstraint(MultiTargetConstraint.SHARE_ARTIFACT_CREATURE_OR_LAND_TYPE));
    }
}
