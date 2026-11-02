package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.condition.TwoOrMoreSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/**
 * Krallenhorde Wantons — back face of Grizzled Outcasts.
 * 7/7 Werewolf.
 * At the beginning of each upkeep, if a player cast two or more spells last turn, transform Krallenhorde Wantons.
 */
public class KrallenhordeWantons extends Card {

    public KrallenhordeWantons() {
        // At the beginning of each upkeep, if a player cast two or more spells last turn, transform Krallenhorde Wantons.
        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                new ConditionalEffect(new TwoOrMoreSpellsCastLastTurn(), new TransformSelfEffect()));
    }
}
