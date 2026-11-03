package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.TwoOrMoreSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/**
 * Howlpack of Estwald — back face of Villagers of Estwald.
 * 4/6 Werewolf.
 * At the beginning of each upkeep, if a player cast two or more spells last turn, transform this creature.
 */
public class HowlpackOfEstwald extends Card {

    public HowlpackOfEstwald() {
        // At the beginning of each upkeep, if a player cast two or more spells last turn, transform this creature.
        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                TriggerOptions.DEFAULT.withInterveningIf(new TwoOrMoreSpellsCastLastTurn()),
                new TransformSelfEffect());
    }
}
