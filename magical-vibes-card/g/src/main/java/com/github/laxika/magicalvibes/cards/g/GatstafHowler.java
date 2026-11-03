package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.condition.TwoOrMoreSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/**
 * Gatstaf Howler — back face of Gatstaf Shepherd.
 * 3/3 Werewolf with Intimidate.
 * At the beginning of each upkeep, if a player cast two or more spells last turn, transform Gatstaf Howler.
 */
public class GatstafHowler extends Card {

    public GatstafHowler() {
        // At the beginning of each upkeep, if a player cast two or more spells last turn, transform Gatstaf Howler.
        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                TriggerOptions.DEFAULT.withInterveningIf(new TwoOrMoreSpellsCastLastTurn()),
                new TransformSelfEffect());
    }
}
