package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TwoOrMoreSpellsCastLastTurnConditionalEffect;

/**
 * Howlpack of Estwald — back face of Villagers of Estwald.
 * 4/6 Werewolf.
 * At the beginning of each upkeep, if a player cast two or more spells last turn, transform Howlpack of Estwald.
 */
public class HowlpackOfEstwald extends Card {

    public HowlpackOfEstwald() {
        // At the beginning of each upkeep, if a player cast two or more spells last turn, transform Howlpack of Estwald.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new TwoOrMoreSpellsCastLastTurnConditionalEffect(new TransformSelfEffect()));
    }
}
