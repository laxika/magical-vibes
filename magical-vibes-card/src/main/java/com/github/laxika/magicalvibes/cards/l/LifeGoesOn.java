package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Morbid;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "HOU", collectorNumber = "121")
public class LifeGoesOn extends Card {

    public LifeGoesOn() {
        // You gain 4 life. If a creature died this turn, you gain 8 life instead.
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(new Morbid(),
                new GainLifeEffect(4),
                new GainLifeEffect(8)
        ));
    }
}
