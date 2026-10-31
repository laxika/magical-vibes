package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BolsterEffect;

@CardRegistration(set = "DTK", collectorNumber = "184")
public class DromokasGift extends Card {

    public DromokasGift() {
        addSpellEffect(new BolsterEffect(4));
    }
}
