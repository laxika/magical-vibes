package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "HBG", collectorNumber = "111")
public class Blur extends Card {

    public Blur() {
        // Exile target creature you control, then return that card to the battlefield
        // under its owner's control. Draw a card.
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.SPELL, FlickerEffect.flickerTarget())
                .addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
