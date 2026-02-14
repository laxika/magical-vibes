package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.UntapSelfEffect;

import java.util.List;

public class HorseshoeCrab extends Card {

    public HorseshoeCrab() {
        super("Horseshoe Crab", CardType.CREATURE, "{2}{U}", CardColor.BLUE);

        setSubtypes(List.of(CardSubtype.CRAB));
        setCardText("{U}: Untap Horseshoe Crab.");
        setPower(1);
        setToughness(3);
        addActivatedAbility(new ActivatedAbility(false, "{U}", List.of(new UntapSelfEffect()), false, "{U}: Untap Horseshoe Crab."));
    }
}
