package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;

@CardRegistration(set = "PCY", collectorNumber = "42")
public class Rethink extends Card {

    public Rethink() {
        addSpellEffect(new CounterUnlessPaysEffect(new TargetSpellManaValue()));
    }
}
