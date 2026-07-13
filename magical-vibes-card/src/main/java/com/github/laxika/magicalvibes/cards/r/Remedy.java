package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDividedDamageEffect;

@CardRegistration(set = "6ED", collectorNumber = "36")
public class Remedy extends Card {

    public Remedy() {
        // Prevent the next 5 damage that would be dealt this turn to any number of targets,
        // divided as you choose. Per-target shields ride on the cast-time damage assignments.
        addEffect(EffectSlot.SPELL, new PreventDividedDamageEffect(5));
    }
}
