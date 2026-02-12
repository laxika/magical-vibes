package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Set;

public class WallOfSwords extends Card {

    public WallOfSwords() {
        super("Wall of Swords", CardType.CREATURE, "{3}{W}", CardColor.WHITE);

        setSubtypes(List.of(CardSubtype.WALL));
        setCardText("Defender\nFlying");
        setKeywords(Set.of(Keyword.DEFENDER, Keyword.FLYING));
        setPower(3);
        setToughness(5);
    }
}
