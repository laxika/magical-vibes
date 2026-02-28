package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfMetalcraftEffect;

@CardRegistration(set = "SOM", collectorNumber = "46")
public class StoicRebuttal extends Card {

    public StoicRebuttal() {
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostIfMetalcraftEffect(1));
    }
}
