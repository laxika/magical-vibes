package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Set;

public class SerraAngel extends Card {

    public SerraAngel() {
        super("Serra Angel", CardType.CREATURE, "{3}{W}{W}", CardColor.WHITE);

        setSubtypes(List.of(CardSubtype.ANGEL));
        setCardText("Flying, vigilance");
        setKeywords(Set.of(Keyword.FLYING, Keyword.VIGILANCE));
        setPower(4);
        setToughness(4);
    }
}
