package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Set;

public class SkyhunterSkirmisher extends Card {

    public SkyhunterSkirmisher() {
        super("Skyhunter Skirmisher", CardType.CREATURE, "{1}{W}{W}", CardColor.WHITE);

        setSubtypes(List.of(CardSubtype.CAT, CardSubtype.KNIGHT));
        setCardText("Flying, double strike");
        setKeywords(Set.of(Keyword.FLYING, Keyword.DOUBLE_STRIKE));
        setPower(1);
        setToughness(1);
    }
}
