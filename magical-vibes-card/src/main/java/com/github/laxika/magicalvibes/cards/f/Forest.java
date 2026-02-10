package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

public class Forest extends Card {

    public Forest() {
        super("Forest", CardType.BASIC_LAND, null);

        setSubtypes(List.of(CardSubtype.FOREST));
        setCardText("{T}: Add {G}.");
        setOnTapEffects(List.of(new AwardManaEffect("G")));
    }
}
