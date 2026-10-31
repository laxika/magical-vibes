package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "MBS", collectorNumber = "25")
public class FuelForTheCause extends Card {

    public FuelForTheCause() {
        addSpellEffect(new CounterSpellEffect());
        addSpellEffect(new ProliferateEffect());
    }
}
