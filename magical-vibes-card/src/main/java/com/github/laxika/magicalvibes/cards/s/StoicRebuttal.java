package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.Metalcraft;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;

@CardRegistration(set = "SOM", collectorNumber = "46")
public class StoicRebuttal extends Card {

    public StoicRebuttal() {
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
        // Stoic Rebuttal costs {1} less to cast if you control three or more artifacts. (metalcraft)
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new Metalcraft(), new ReduceOwnCastCostEffect(new Fixed(1))));
    }
}
