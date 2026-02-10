package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Set;

public class GiantSpider extends Card {

    public GiantSpider() {
        super("Giant Spider", CardType.CREATURE, "{3}{G}", CardColor.GREEN);

        setSubtypes(List.of(CardSubtype.SPIDER));
        setPower(2);
        setToughness(4);
        setKeywords(Set.of(Keyword.REACH));
    }
}
