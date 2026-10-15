package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.condition.TwoOrMoreSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;

/**
 * Krallenhorde Wantons — back face of Grizzled Outcasts.
 * 7/7 Werewolf.
 * At the beginning of each upkeep, if a player cast two or more spells last turn, transform Krallenhorde Wantons.
 */
public class KrallenhordeWantons extends Card {

    public KrallenhordeWantons() {
        // At the beginning of each upkeep, if a player cast two or more spells last turn, transform Krallenhorde Wantons.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new ConditionalEffect(new TwoOrMoreSpellsCastLastTurn(), new TransformSelfEffect()));
    }
}
