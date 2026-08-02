package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAndExileFromZonesEffect;

import java.util.List;

@CardRegistration(set = "M15", collectorNumber = "117")
public class StainTheMind extends Card {

    public StainTheMind() {
        addEffect(EffectSlot.SPELL, new ChooseCardNameAndExileFromZonesEffect(List.of(CardType.LAND)));
    }
}
