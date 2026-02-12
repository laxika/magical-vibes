package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Set;

public class TrueBeliever extends Card {

    public TrueBeliever() {
        super("True Believer", CardType.CREATURE, "{W}{W}", CardColor.WHITE);

        setSubtypes(List.of(CardSubtype.HUMAN, CardSubtype.CLERIC));
        setCardText("You have shroud. (You can't be the target of spells or abilities.)");
        setKeywords(Set.of(Keyword.SHROUD));
        setPower(2);
        setToughness(2);
    }
}
