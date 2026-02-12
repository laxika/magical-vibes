package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

public class Swamp extends Card {

    public Swamp() {
        super("Swamp", CardType.BASIC_LAND, null, CardColor.BLACK);

        setSubtypes(List.of(CardSubtype.SWAMP));
        setCardText("{T}: Add {B}.");
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect("B"));
    }
}
