package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.condition.TwoOrMoreSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/**
 * Rampaging Werewolf — back face of Tormented Pariah.
 * 6/4 Werewolf.
 * At the beginning of each upkeep, if a player cast two or more spells last turn, transform Rampaging Werewolf.
 */
public class RampagingWerewolf extends Card {

    public RampagingWerewolf() {
        // At the beginning of each upkeep, if a player cast two or more spells last turn, transform Rampaging Werewolf.
        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                TriggerOptions.DEFAULT.withInterveningIf(new TwoOrMoreSpellsCastLastTurn()),
                new TransformSelfEffect());
    }
}
