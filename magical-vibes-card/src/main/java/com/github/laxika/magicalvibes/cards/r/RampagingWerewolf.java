package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TwoOrMoreSpellsCastLastTurnConditionalEffect;

/**
 * Rampaging Werewolf — back face of Tormented Pariah.
 * 6/4 Werewolf.
 * At the beginning of each upkeep, if a player cast two or more spells last turn, transform Rampaging Werewolf.
 */
public class RampagingWerewolf extends Card {

    public RampagingWerewolf() {
        // At the beginning of each upkeep, if a player cast two or more spells last turn, transform Rampaging Werewolf.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new TwoOrMoreSpellsCastLastTurnConditionalEffect(new TransformSelfEffect()));
    }
}
