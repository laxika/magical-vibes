package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Set;

public class AngelicWall extends Card {

    public AngelicWall() {
        super("Angelic Wall", CardType.CREATURE, "{1}{W}", CardColor.WHITE);

        setSubtypes(List.of(CardSubtype.WALL));
        setCardText("Defender\nFlying");
        setKeywords(Set.of(Keyword.DEFENDER, Keyword.FLYING));
        setPower(0);
        setToughness(4);
    }
}
