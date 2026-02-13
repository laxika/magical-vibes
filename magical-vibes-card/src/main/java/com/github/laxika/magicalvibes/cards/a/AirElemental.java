package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Set;

public class AirElemental extends Card {

    public AirElemental() {
        super("Air Elemental", CardType.CREATURE, "{3}{U}{U}", CardColor.BLUE);

        setSubtypes(List.of(CardSubtype.ELEMENTAL));
        setCardText("Flying");
        setKeywords(Set.of(Keyword.FLYING));
        setPower(4);
        setToughness(4);
    }
}
