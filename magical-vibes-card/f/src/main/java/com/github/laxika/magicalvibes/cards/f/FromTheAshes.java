package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSearchesLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "C13", collectorNumber = "108")
public class FromTheAshes extends Card {

    public FromTheAshes() {
        // Destroy all nonbasic lands. For each land destroyed this way, its controller may search their library for a
        // basic land card and put it onto the battlefield. Then each player who searched their library this way shuffles.
        addSpellEffect(DestroyTargetPermanentEffect.all(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsLandPredicate(),
                        new PermanentNotPredicate(new PermanentHasSupertypePredicate(CardSupertype.BASIC))
                ))));
        addSpellEffect(EachPlayerSearchesLibraryEffect.maySearch(
                Subjects.playersThisWay(StepKind.DESTROY),
                CardPredicateUtils.basicLand(),
                new AmountThisWay(StepKind.DESTROY).controlledByYou(),
                LibrarySearchDestination.BATTLEFIELD).oneSearchForEach());
    }
}
