package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAndExileFromZonesEffect;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "69")
public class Memoricide extends Card {

    public Memoricide() {
        addEffect(EffectSlot.SPELL, new ChooseCardNameAndExileFromZonesEffect(List.of(CardType.LAND)));
    }
}
