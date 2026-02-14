package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;

import java.util.List;

public class FugitiveWizard extends Card {

    public FugitiveWizard() {
        super("Fugitive Wizard", CardType.CREATURE, "{U}", CardColor.BLUE);

        setSubtypes(List.of(CardSubtype.HUMAN, CardSubtype.WIZARD));
        setPower(1);
        setToughness(1);
    }
}
