package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnControlledCreatureOrPayManaCost;

@CardRegistration(set = "ECL", collectorNumber = "84")
public class WildUnraveling extends Card {

    public WildUnraveling() {
        addEffect(EffectSlot.SPELL,
                new PutCountersOnControlledCreatureOrPayManaCost(CounterType.MINUS_ONE_MINUS_ONE, 2, "{1}"));
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
