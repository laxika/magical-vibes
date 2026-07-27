package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;


@CardRegistration(set = "ALA", collectorNumber = "103")
public class HellsThunder extends Card {

    public HellsThunder() {
        // At the beginning of the end step, sacrifice this creature.
        addEffect(EffectSlot.END_STEP_TRIGGERED, new SacrificeSelfEffect());

        // Unearth {4}{R}: Return this card from your graveyard to the battlefield. It gains haste.
        // Exile it at the beginning of the next end step. Unearth only as a sorcery.
        addUnearth("{4}{R}");
    }
}
