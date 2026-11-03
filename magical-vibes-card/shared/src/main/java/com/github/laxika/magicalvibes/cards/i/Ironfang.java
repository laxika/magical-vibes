package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.condition.TwoOrMoreSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/**
 * Ironfang — back face of Village Ironsmith.
 * 3/1 Werewolf with first strike.
 * At the beginning of each upkeep, if a player cast two or more spells last turn, transform Ironfang.
 */
public class Ironfang extends Card {

    public Ironfang() {
        // At the beginning of each upkeep, if a player cast two or more spells last turn, transform Ironfang.
        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                TriggerOptions.DEFAULT.withInterveningIf(new TwoOrMoreSpellsCastLastTurn()),
                new TransformSelfEffect());
    }
}
