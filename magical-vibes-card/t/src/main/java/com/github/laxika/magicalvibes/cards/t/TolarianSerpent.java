package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/**
 * Tolarian Serpent — {5}{U}{U} Creature — Serpent 7/7.
 * "At the beginning of your upkeep, mill seven cards."
 */
@CardRegistration(set = "WTH", collectorNumber = "57")
public class TolarianSerpent extends Card {

    public TolarianSerpent() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MillEffect(7, MillRecipient.CONTROLLER));
    }
}
