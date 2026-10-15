package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TargetSpellManaSpentLessThanManaValue;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "EOE", collectorNumber = "83")
public class Unravel extends Card {

    public Unravel() {
        // The draw condition reads the target spell while it is still on the stack, so it must
        // resolve before the counter removes that spell.
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new TargetSpellManaSpentLessThanManaValue(), new DrawCardEffect()));
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
