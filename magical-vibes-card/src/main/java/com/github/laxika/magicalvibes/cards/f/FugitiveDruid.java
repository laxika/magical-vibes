package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

/**
 * Fugitive Druid — "Whenever this creature becomes the target of an Aura spell, you draw a card."
 * The Aura's controller is irrelevant; the draw always goes to the Druid's controller.
 */
@CardRegistration(set = "TMP", collectorNumber = "229")
public class FugitiveDruid extends Card {

    public FugitiveDruid() {
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_AURA_SPELL, new DrawCardEffect());
    }
}
