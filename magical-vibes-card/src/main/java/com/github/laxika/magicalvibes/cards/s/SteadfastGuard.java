package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Set;

public class SteadfastGuard extends Card {

    public SteadfastGuard() {
        super("Steadfast Guard", CardType.CREATURE, "{W}{W}", CardColor.WHITE);

        setSubtypes(List.of(CardSubtype.HUMAN, CardSubtype.REBEL));
        setCardText("Vigilance");
        setKeywords(Set.of(Keyword.VIGILANCE));
        setPower(2);
        setToughness(2);
    }
}
