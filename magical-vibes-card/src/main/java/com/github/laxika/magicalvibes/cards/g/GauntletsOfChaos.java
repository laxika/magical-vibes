package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfSharedTypeTargetsAndDestroyAurasEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "373")
public class GauntletsOfChaos extends Card {

    public GauntletsOfChaos() {
        // {5}, Sacrifice this artifact: Exchange control of target artifact, creature, or land you
        // control and target permanent an opponent controls that shares one of those types with it.
        // If those permanents are exchanged this way, destroy all Auras attached to them.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}",
                List.of(new SacrificeSelfCost(), new ExchangeControlOfSharedTypeTargetsAndDestroyAurasEffect()),
                "{5}, Sacrifice this artifact: Exchange control of target artifact, creature, or land you "
                        + "control and target permanent an opponent controls that shares one of those types "
                        + "with it. If those permanents are exchanged this way, destroy all Auras attached to them.",
                List.of(
                        new ControlledPermanentPredicateTargetFilter(
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsArtifactPredicate(),
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentIsLandPredicate())),
                                "Target must be an artifact, creature, or land you control"),
                        new PermanentPredicateTargetFilter(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
                                        new PermanentAnyOfPredicate(List.of(
                                                new PermanentIsArtifactPredicate(),
                                                new PermanentIsCreaturePredicate(),
                                                new PermanentIsLandPredicate())))),
                                "Target must be an artifact, creature, or land an opponent controls")
                ),
                2,
                2
        ).withMultiTargetConstraint(MultiTargetConstraint.SHARE_ARTIFACT_CREATURE_OR_LAND_TYPE));
    }
}
