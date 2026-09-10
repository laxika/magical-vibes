package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "LGN", collectorNumber = "62")
@CardRegistration(set = "HOP", collectorNumber = "22")
public class CorpseHarvester extends Card {

    public CorpseHarvester() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{B}",
                List.of(
                        new SacrificeCreatureCost(),
                        new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.ZOMBIE), LibrarySearchDestination.HAND),
                        new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.SWAMP), LibrarySearchDestination.HAND)
                ),
                "{1}{B}, {T}, Sacrifice a creature: Search your library for a Zombie card and a Swamp card, reveal them, put them into your hand, then shuffle."
        ));
    }
}
