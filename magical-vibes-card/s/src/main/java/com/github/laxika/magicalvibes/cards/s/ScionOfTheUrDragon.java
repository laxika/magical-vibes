package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfSubjectEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "246")
@CardRegistration(set = "SLD", collectorNumber = "1531")
@CardRegistration(set = "C17", collectorNumber = "192")
public class ScionOfTheUrDragon extends Card {

    public ScionOfTheUrDragon() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new SearchLibraryEffect(new CardAllOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.DRAGON),
                                new CardIsPermanentPredicate())), LibrarySearchDestination.GRAVEYARD),
                        new ConditionalEffect(HappenedThisWay.searched(),
                                BecomeCopyOfSubjectEffect.of(Subjects.foundThisWay()).untilEndOfTurn())),
                "{2}: Search your library for a Dragon permanent card and put it into your graveyard. "
                        + "If you do, Scion of the Ur-Dragon becomes a copy of that card until end of turn. "
                        + "Then shuffle."));
    }
}
