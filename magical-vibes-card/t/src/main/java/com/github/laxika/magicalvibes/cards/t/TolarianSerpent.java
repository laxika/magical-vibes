package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

/**
 * Tolarian Serpent — {5}{U}{U} Creature — Serpent 7/7.
 * "At the beginning of your upkeep, mill seven cards."
 */
@CardRegistration(set = "WTH", collectorNumber = "57")
public class TolarianSerpent extends Card {

    public TolarianSerpent() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MillEffect(7, MillRecipient.CONTROLLER));
    }
}
