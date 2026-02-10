package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;

import java.util.List;

public class GrizzlyBears extends Card {

    public GrizzlyBears() {
        super("Grizzly Bears", CardType.CREATURE, "{1}{G}", CardColor.GREEN);

        setSubtypes(List.of(CardSubtype.BEAR));
        setPower(2);
        setToughness(2);
    }
}
