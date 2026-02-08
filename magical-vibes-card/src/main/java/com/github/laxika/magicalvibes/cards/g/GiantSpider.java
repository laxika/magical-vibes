package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Set;

public class GiantSpider extends Card {

    public GiantSpider() {
        super("Giant Spider", CardType.CREATURE, List.of(CardSubtype.SPIDER), null, List.of(), "{3}{G}", 2, 4, Set.of(Keyword.REACH));
    }
}
