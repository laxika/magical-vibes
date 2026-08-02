package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAndExileFromZonesEffect;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "105")
public class CranialExtraction extends Card {

    public CranialExtraction() {
        addEffect(EffectSlot.SPELL, new ChooseCardNameAndExileFromZonesEffect(List.of(CardType.LAND)));
    }
}
