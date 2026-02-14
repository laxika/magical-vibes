package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

public class Plains extends Card {

    public Plains() {
        super("Plains", CardType.BASIC_LAND, null, CardColor.WHITE);

        setSubtypes(List.of(CardSubtype.PLAINS));
        setCardText("{T}: Add {W}.");
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.WHITE));
    }
}
