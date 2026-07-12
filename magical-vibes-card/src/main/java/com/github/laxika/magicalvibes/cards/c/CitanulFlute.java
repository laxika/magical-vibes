package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "315")
public class CitanulFlute extends Card {

    public CitanulFlute() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}",
                List.of(new SearchLibraryEffect(new CardTypePredicate(CardType.CREATURE),
                        LibrarySearchDestination.HAND, new ManaValueBound(false, 0))),
                "{X}, {T}: Search your library for a creature card with mana value X or less, reveal it, put it into your hand, then shuffle."
        ));
    }
}
