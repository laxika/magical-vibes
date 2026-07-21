package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "HOU", collectorNumber = "115")
public class FeralProwler extends Card {

    public FeralProwler() {
        // When this creature dies, draw a card.
        addEffect(EffectSlot.ON_DEATH, new DrawCardEffect());
    }
}
