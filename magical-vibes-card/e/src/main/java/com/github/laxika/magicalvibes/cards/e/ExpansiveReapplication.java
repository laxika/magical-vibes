package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ConjureRandomCreatureWithManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;

@CardRegistration(set = "YSOS", collectorNumber = "18")
public class ExpansiveReapplication extends Card {

    public ExpansiveReapplication() {
        setMinimumXValue(1);
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
        addEffect(EffectSlot.SPELL,
                new ConjureRandomCreatureWithManaValueEffect(new XValue(), false));
    }
}
