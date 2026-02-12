package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Set;

public class SkyhunterPatrol extends Card {

    public SkyhunterPatrol() {
        super("Skyhunter Patrol", CardType.CREATURE, "{2}{W}{W}", CardColor.WHITE);

        setSubtypes(List.of(CardSubtype.CAT, CardSubtype.KNIGHT));
        setCardText("Flying, first strike");
        setKeywords(Set.of(Keyword.FLYING, Keyword.FIRST_STRIKE));
        setPower(2);
        setToughness(3);
    }
}
