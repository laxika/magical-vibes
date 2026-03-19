package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TwoOrMoreSpellsCastLastTurnConditionalEffect;

/**
 * Ironfang — back face of Village Ironsmith.
 * 3/1 Werewolf with first strike.
 * At the beginning of each upkeep, if a player cast two or more spells last turn, transform Ironfang.
 */
public class Ironfang extends Card {

    public Ironfang() {
        // At the beginning of each upkeep, if a player cast two or more spells last turn, transform Ironfang.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new TwoOrMoreSpellsCastLastTurnConditionalEffect(new TransformSelfEffect()));
    }
}
