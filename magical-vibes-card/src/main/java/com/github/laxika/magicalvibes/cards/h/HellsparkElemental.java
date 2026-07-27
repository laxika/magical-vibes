package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;


@CardRegistration(set = "CON", collectorNumber = "65")
public class HellsparkElemental extends Card {

    public HellsparkElemental() {
        // At the beginning of the end step, sacrifice this creature.
        addEffect(EffectSlot.END_STEP_TRIGGERED, new SacrificeSelfEffect());

        // Unearth {1}{R}: Return this card from your graveyard to the battlefield. It gains haste.
        // Exile it at the beginning of the next end step. Unearth only as a sorcery.
        addUnearth("{1}{R}");
    }
}
