package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellIfControllerPoisonedEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;

@CardRegistration(set = "ONE", collectorNumber = "44")
public class BringTheEnding extends Card {

    public BringTheEnding() {
        addSpellEffect(new CounterSpellIfControllerPoisonedEffect(3));
        addSpellEffect(new CounterUnlessPaysEffect(2));
    }
}
