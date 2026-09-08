package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysLifeEffect;

@CardRegistration(set = "STX", collectorNumber = "210")
public class OwlinShieldmage extends Card {

    public OwlinShieldmage() {
        // Flying is auto-loaded from Scryfall.
        // Ward—Pay 3 life.
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                new CounterUnlessPaysLifeEffect(new Fixed(3)));
    }
}
