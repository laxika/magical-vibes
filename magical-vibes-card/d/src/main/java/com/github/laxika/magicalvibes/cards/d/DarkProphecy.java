package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;

@CardRegistration(set = "M14", collectorNumber = "93")
public class DarkProphecy extends Card {

    public DarkProphecy() {
        // Whenever a creature you control dies, you draw a card and you lose 1 life.
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new DrawCardEffect(1));
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new LoseLifeEffect(1));
    }
}
