package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;

import java.util.List;
import java.util.Set;

public class GrizzlyBears extends Card {

    public GrizzlyBears() {
        super("Grizzly Bears", CardType.CREATURE, List.of(CardSubtype.BEAR), null, List.of(), "{1}{G}", 2, 2, Set.of());
    }
}
