package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TwoOrMoreSpellsCastLastTurnConditionalEffect;

/**
 * Gatstaf Howler — back face of Gatstaf Shepherd.
 * 3/3 Werewolf with Intimidate.
 * At the beginning of each upkeep, if a player cast two or more spells last turn, transform Gatstaf Howler.
 */
public class GatstafHowler extends Card {

    public GatstafHowler() {
        // At the beginning of each upkeep, if a player cast two or more spells last turn, transform Gatstaf Howler.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new TwoOrMoreSpellsCastLastTurnConditionalEffect(new TransformSelfEffect()));
    }
}
