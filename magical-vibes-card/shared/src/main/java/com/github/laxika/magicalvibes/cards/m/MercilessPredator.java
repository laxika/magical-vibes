package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.condition.TwoOrMoreSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;

/**
 * Merciless Predator — back face of Reckless Waif.
 * 3/2 Werewolf.
 * At the beginning of each upkeep, if a player cast two or more spells last turn, transform Merciless Predator.
 */
public class MercilessPredator extends Card {

    public MercilessPredator() {
        // At the beginning of each upkeep, if a player cast two or more spells last turn, transform Merciless Predator.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new ConditionalEffect(new TwoOrMoreSpellsCastLastTurn(), new TransformSelfEffect()));
    }
}
