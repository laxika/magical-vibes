package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSearchesLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "C14", collectorNumber = "51")
@CardRegistration(set = "CMA", collectorNumber = "165")
public class WaveOfVitriol extends Card {

    public WaveOfVitriol() {
        // Each player sacrifices all artifacts, enchantments, and nonbasic lands they control.
        addSpellEffect(new SacrificeEachMatchingPermanentEffect(new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsEnchantmentPredicate(),
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsLandPredicate(),
                        new PermanentNotPredicate(new PermanentHasSupertypePredicate(CardSupertype.BASIC))))))));
        // For each land sacrificed this way, its controller may search their library for a basic land card and put it
        // onto the battlefield tapped. Then each player who searched their library this way shuffles.
        addSpellEffect(EachPlayerSearchesLibraryEffect.maySearch(
                Subjects.playersThisWay(StepKind.SACRIFICE),
                CardPredicateUtils.basicLand(),
                new AmountThisWay(StepKind.SACRIFICE).matching(new PermanentIsLandPredicate()).controlledByYou(),
                LibrarySearchDestination.BATTLEFIELD_TAPPED).oneSearchForEach());
    }
}
