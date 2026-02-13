package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Set;

public class YouthfulKnight extends Card {

    public YouthfulKnight() {
        super("Youthful Knight", CardType.CREATURE, "{1}{W}", CardColor.WHITE);

        setSubtypes(List.of(CardSubtype.HUMAN, CardSubtype.KNIGHT));
        setCardText("First strike");
        setKeywords(Set.of(Keyword.FIRST_STRIKE));
        setPower(2);
        setToughness(1);
    }
}
