package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "DTK", collectorNumber = "84")
public class YouthfulScholar extends Card {

    public YouthfulScholar() {
        // When this creature dies, draw two cards.
        addEffect(EffectSlot.ON_DEATH, new DrawCardEffect(2));
    }
}
