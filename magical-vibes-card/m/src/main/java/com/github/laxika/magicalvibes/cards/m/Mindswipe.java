package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

@CardRegistration(set = "KTK", collectorNumber = "189")
public class Mindswipe extends Card {

    public Mindswipe() {
        // Counter target spell unless its controller pays {X}. Mindswipe deals X damage to that spell's controller.
        addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(0, true, false));
        addEffect(EffectSlot.SPELL, new DealDamageToPlayersEffect(
                new XValue(), DamageRecipient.TARGET_SPELL_CONTROLLER));
    }
}
