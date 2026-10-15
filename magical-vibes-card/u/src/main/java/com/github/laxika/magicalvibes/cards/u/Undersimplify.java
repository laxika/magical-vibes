package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostTargetCreatureSpellEffect;

@CardRegistration(set = "HBG", collectorNumber = "40")
public class Undersimplify extends Card {

    public Undersimplify() {
        addEffect(EffectSlot.SPELL, new PerpetuallyBoostTargetCreatureSpellEffect(-2, 0));
        addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(2));
    }
}
