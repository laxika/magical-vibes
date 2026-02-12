package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Set;

public class TundraWolves extends Card {

    public TundraWolves() {
        super("Tundra Wolves", CardType.CREATURE, "{W}", CardColor.WHITE);

        setSubtypes(List.of(CardSubtype.WOLF));
        setCardText("First strike");
        setKeywords(Set.of(Keyword.FIRST_STRIKE));
        setPower(1);
        setToughness(1);
    }
}
