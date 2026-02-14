package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;

import java.util.List;

public class LumengridWarden extends Card {

    public LumengridWarden() {
        super("Lumengrid Warden", CardType.CREATURE, "{1}{U}", CardColor.BLUE);

        setSubtypes(List.of(CardSubtype.HUMAN, CardSubtype.WIZARD));
        setPower(1);
        setToughness(3);
    }
}
