package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Set;

public class SnappingDrake extends Card {

    public SnappingDrake() {
        super("Snapping Drake", CardType.CREATURE, "{3}{U}", CardColor.BLUE);

        setSubtypes(List.of(CardSubtype.DRAKE));
        setCardText("Flying");
        setKeywords(Set.of(Keyword.FLYING));
        setPower(3);
        setToughness(2);
    }
}
