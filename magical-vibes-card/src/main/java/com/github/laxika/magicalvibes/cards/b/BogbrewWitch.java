package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;

@CardRegistration(set = "M14", collectorNumber = "88")
public class BogbrewWitch extends Card {

    public BogbrewWitch() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new SearchLibraryEffect(
                        new CardAnyOfPredicate(List.<CardPredicate>of(
                                new CardNamedPredicate("Festering Newt"),
                                new CardNamedPredicate("Bubbling Cauldron"))),
                        LibrarySearchDestination.BATTLEFIELD_TAPPED)),
                "{2}, {T}: Search your library for a card named Festering Newt or Bubbling Cauldron, "
                        + "put it onto the battlefield tapped, then shuffle."
        ));
    }
}
