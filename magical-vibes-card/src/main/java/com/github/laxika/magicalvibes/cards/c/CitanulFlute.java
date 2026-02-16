package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCreatureWithMVXOrLessToHandEffect;

import java.util.List;

public class CitanulFlute extends Card {

    public CitanulFlute() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}",
                List.of(new SearchLibraryForCreatureWithMVXOrLessToHandEffect()),
                false,
                "{X}, {T}: Search your library for a creature card with mana value X or less, reveal it, put it into your hand, then shuffle."
        ));
    }
}
