package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostTargetCreatureSpellEffect;

@CardRegistration(set = "HBG", collectorNumber = "40")
public class Undersimplify extends Card {

    public Undersimplify() {
        addSpellEffect(new PerpetuallyBoostTargetCreatureSpellEffect(-2, 0));
        addSpellEffect(new CounterUnlessPaysEffect(2));
    }
}
