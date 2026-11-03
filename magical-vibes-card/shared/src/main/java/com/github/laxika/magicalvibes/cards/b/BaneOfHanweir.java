package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.condition.TwoOrMoreSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/**
 * Bane of Hanweir — back face of Hanweir Watchkeep.
 * 5/5 Werewolf.
 * Bane of Hanweir attacks each combat if able.
 * At the beginning of each upkeep, if a player cast two or more spells last turn, transform Bane of Hanweir.
 */
public class BaneOfHanweir extends Card {

    public BaneOfHanweir() {
        // Bane of Hanweir attacks each combat if able.
        addStaticEffect(new MustAttackEffect());

        // At the beginning of each upkeep, if a player cast two or more spells last turn, transform Bane of Hanweir.
        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                TriggerOptions.DEFAULT.withInterveningIf(new TwoOrMoreSpellsCastLastTurn()),
                new TransformSelfEffect());
    }
}
