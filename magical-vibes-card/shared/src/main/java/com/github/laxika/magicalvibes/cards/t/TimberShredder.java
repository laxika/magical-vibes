package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.TwoOrMoreSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/**
 * Timber Shredder — back face of Hinterland Logger.
 * 4/2 Werewolf with Trample.
 * At the beginning of each upkeep, if a player cast two or more spells last turn, transform this creature.
 */
public class TimberShredder extends Card {

    public TimberShredder() {
        // Trample is loaded from Scryfall.

        // At the beginning of each upkeep, if a player cast two or more spells last turn, transform this creature.
        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                new ConditionalEffect(new TwoOrMoreSpellsCastLastTurn(), new TransformSelfEffect()));
    }
}
