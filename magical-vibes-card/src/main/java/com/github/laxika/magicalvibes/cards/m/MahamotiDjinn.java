package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Set;

public class MahamotiDjinn extends Card {

    public MahamotiDjinn() {
        super("Mahamoti Djinn", CardType.CREATURE, "{4}{U}{U}", CardColor.BLUE);

        setSubtypes(List.of(CardSubtype.DJINN));
        setCardText("Flying");
        setKeywords(Set.of(Keyword.FLYING));
        setPower(5);
        setToughness(6);
    }
}
