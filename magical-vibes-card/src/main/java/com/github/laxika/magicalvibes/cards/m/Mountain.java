package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

public class Mountain extends Card {

    public Mountain() {
        super("Mountain", CardType.BASIC_LAND, null, CardColor.RED);

        setSubtypes(List.of(CardSubtype.MOUNTAIN));
        setCardText("{T}: Add {R}.");
        setOnTapEffects(List.of(new AwardManaEffect("R")));
    }
}
