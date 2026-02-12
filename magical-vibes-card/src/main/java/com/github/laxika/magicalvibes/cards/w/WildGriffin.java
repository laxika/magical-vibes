package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Set;

public class WildGriffin extends Card {

    public WildGriffin() {
        super("Wild Griffin", CardType.CREATURE, "{2}{W}", CardColor.WHITE);

        setSubtypes(List.of(CardSubtype.GRIFFIN));
        setCardText("Flying");
        setKeywords(Set.of(Keyword.FLYING));
        setPower(2);
        setToughness(2);
    }
}
