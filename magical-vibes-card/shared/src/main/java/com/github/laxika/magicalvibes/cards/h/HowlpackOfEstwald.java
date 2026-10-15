package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TwoOrMoreSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

/**
 * Howlpack of Estwald — back face of Villagers of Estwald.
 * 4/6 Werewolf.
 * At the beginning of each upkeep, if a player cast two or more spells last turn, transform this creature.
 */
public class HowlpackOfEstwald extends Card {

    public HowlpackOfEstwald() {
        // At the beginning of each upkeep, if a player cast two or more spells last turn, transform this creature.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new ConditionalEffect(new TwoOrMoreSpellsCastLastTurn(), new TransformSelfEffect()));
    }
}
