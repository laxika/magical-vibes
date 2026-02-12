package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Set;

public class SuntailHawk extends Card {

    public SuntailHawk() {
        super("Suntail Hawk", CardType.CREATURE, "{W}", CardColor.WHITE);

        setSubtypes(List.of(CardSubtype.BIRD));
        setCardText("Flying");
        setKeywords(Set.of(Keyword.FLYING));
        setPower(1);
        setToughness(1);
    }
}
