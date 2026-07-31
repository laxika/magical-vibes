package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;

import java.util.List;

/**
 * The "A deck can have any number of cards named Shadowborn Apostle" line is a deck-construction
 * rule with no in-game engine behaviour, so only the activated ability is modelled. The six
 * sacrificed Apostles may include this one — the cost does not exclude the source.
 */
@CardRegistration(set = "M14", collectorNumber = "114")
public class ShadowbornApostle extends Card {

    public ShadowbornApostle() {
        // {B}, Sacrifice six creatures named Shadowborn Apostle: Search your library for a Demon creature card,
        // put it onto the battlefield, then shuffle.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(
                        new SacrificeMultiplePermanentsCost(6, new PermanentNamedPredicate("Shadowborn Apostle")),
                        new SearchLibraryEffect(
                                new CardAllOfPredicate(List.of(
                                        new CardTypePredicate(CardType.CREATURE),
                                        new CardSubtypePredicate(CardSubtype.DEMON))),
                                LibrarySearchDestination.BATTLEFIELD)
                ),
                "{B}, Sacrifice six creatures named Shadowborn Apostle: Search your library for a Demon creature card, "
                        + "put it onto the battlefield, then shuffle."
        ));
    }
}
