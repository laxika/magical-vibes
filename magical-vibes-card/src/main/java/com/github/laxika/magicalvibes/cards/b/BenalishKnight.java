package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Set;

public class BenalishKnight extends Card {

    public BenalishKnight() {
        super("Benalish Knight", CardType.CREATURE, "{2}{W}", CardColor.WHITE);

        setSubtypes(List.of(CardSubtype.HUMAN, CardSubtype.KNIGHT));
        setCardText("Flash\nFirst strike");
        setKeywords(Set.of(Keyword.FLASH, Keyword.FIRST_STRIKE));
        setPower(2);
        setToughness(2);
    }
}
