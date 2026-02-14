package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

public class Island extends Card {

    public Island() {
        super("Island", CardType.BASIC_LAND, null, CardColor.BLUE);

        setSubtypes(List.of(CardSubtype.ISLAND));
        setCardText("{T}: Add {U}.");
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.BLUE));
    }
}
