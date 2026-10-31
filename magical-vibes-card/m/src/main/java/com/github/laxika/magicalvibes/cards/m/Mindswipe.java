package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

@CardRegistration(set = "KTK", collectorNumber = "189")
public class Mindswipe extends Card {

    public Mindswipe() {
        // Counter target spell unless its controller pays {X}. Mindswipe deals X damage to that spell's controller.
        addSpellEffect(new CounterUnlessPaysEffect(0, true, false));
        addSpellEffect(new DealDamageToPlayersEffect(
                new XValue(), DamageRecipient.TARGET_SPELL_CONTROLLER));
    }
}
