package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Set;

public class SkyhunterProwler extends Card {

    public SkyhunterProwler() {
        super("Skyhunter Prowler", CardType.CREATURE, "{2}{W}", CardColor.WHITE);

        setSubtypes(List.of(CardSubtype.CAT, CardSubtype.KNIGHT));
        setCardText("Flying, vigilance");
        setKeywords(Set.of(Keyword.FLYING, Keyword.VIGILANCE));
        setPower(1);
        setToughness(3);
    }
}
