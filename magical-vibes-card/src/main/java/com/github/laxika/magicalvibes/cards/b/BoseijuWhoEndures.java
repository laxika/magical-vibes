package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceActivationCostEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "266")
public class BoseijuWhoEndures extends Card {

    public BoseijuWhoEndures() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));

        PermanentPredicate nonbasicLand = new PermanentAllOfPredicate(List.of(
                new PermanentIsLandPredicate(),
                new PermanentNotPredicate(new PermanentHasSupertypePredicate(CardSupertype.BASIC))
        ));
        PermanentPredicate destroyablePermanent = new PermanentAllOfPredicate(List.of(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsEnchantmentPredicate(),
                        nonbasicLand
                )),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
        ));
        PermanentPredicateTargetFilter target = new PermanentPredicateTargetFilter(
                destroyablePermanent,
                "Target must be an artifact, enchantment, or nonbasic land an opponent controls"
        );

        CardAnyOfPredicate basicLandType = new CardAnyOfPredicate(List.of(
                new CardSubtypePredicate(CardSubtype.PLAINS),
                new CardSubtypePredicate(CardSubtype.ISLAND),
                new CardSubtypePredicate(CardSubtype.SWAMP),
                new CardSubtypePredicate(CardSubtype.MOUNTAIN),
                new CardSubtypePredicate(CardSubtype.FOREST)
        ));
        CardAllOfPredicate landWithBasicLandType = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.LAND),
                basicLandType
        ));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(
                        new ReduceActivationCostEffect(new PermanentCount(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY)
                                )),
                                CountScope.CONTROLLER
                        )),
                        new DestroyTargetPermanentThenEffect(
                                new SearchLibraryEffect(landWithBasicLandType, LibrarySearchDestination.BATTLEFIELD),
                                ThenEffectRecipient.TARGET_CONTROLLER
                        )
                ),
                "Channel — {1}{G}, Discard this card: Destroy target artifact, enchantment, or nonbasic land an opponent controls. That player may search their library for a land card with a basic land type, put it onto the battlefield, then shuffle.",
                target
        ));
    }
}
