package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect;

@CardRegistration(set = "ORI", collectorNumber = "17")
public class HealingHands extends Card {

    public HealingHands() {
        // Target player gains 4 life.
        addSpellEffect(new TargetPlayerGainsLifeEffect(4));
        // Draw a card.
        addSpellEffect(new DrawCardEffect(1));
    }
}
