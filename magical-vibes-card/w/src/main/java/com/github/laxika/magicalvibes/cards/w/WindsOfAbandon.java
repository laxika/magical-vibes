package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.condition.Overloaded;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMaySearchLibraryForBasicLandsToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "882")
@CardRegistration(set = "SLD", collectorNumber = "1817")
@CardRegistration(set = "MH1", collectorNumber = "37")
@CardRegistration(set = "SOA", collectorNumber = "12")
public class WindsOfAbandon extends Card {

    public WindsOfAbandon() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{4}{W}{W}"))));
        addSpellEffect(new ConditionalReplacementEffect(
                new Overloaded(),
                SequenceEffect.of(
                        new ExileTargetPermanentEffect(),
                        new ConditionalEffect(HappenedThisWay.exiled(), new SearchLibraryEffect(
                                CardPredicateUtils.basicLand(),
                                LibrarySearchDestination.BATTLEFIELD_TAPPED)
                                .searchedBy(Subjects.controllerOf(Subjects.target())))),
                SequenceEffect.of(
                        new ExileAllPermanentsEffect(opponentCreatures()),
                        EachPlayerMaySearchLibraryForBasicLandsToBattlefieldEffect.mandatoryTapped(
                                new AmountThisWay(StepKind.EXILE).controlledByYou(),
                                Subjects.playersThisWay(StepKind.EXILE)))));
        target(TargetFilters.creatureAnOpponentControls());
    }

    private static PermanentAllOfPredicate opponentCreatures() {
        return new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
    }
}
