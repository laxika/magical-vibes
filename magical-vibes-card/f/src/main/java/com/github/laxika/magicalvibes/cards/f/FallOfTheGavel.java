package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "RTR", collectorNumber = "161")
public class FallOfTheGavel extends Card {

    public FallOfTheGavel() {
        // Counter target spell. You gain 5 life.
        addSpellEffect(new CounterSpellEffect());
        addSpellEffect(new GainLifeEffect(5));
    }
}
