package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TwoOrMoreSpellsCastLastTurnConditionalEffect;

/**
 * Bane of Hanweir — back face of Hanweir Watchkeep.
 * 5/5 Werewolf.
 * Bane of Hanweir attacks each combat if able.
 * At the beginning of each upkeep, if a player cast two or more spells last turn, transform Bane of Hanweir.
 */
public class BaneOfHanweir extends Card {

    public BaneOfHanweir() {
        // Bane of Hanweir attacks each combat if able.
        addEffect(EffectSlot.STATIC, new MustAttackEffect());

        // At the beginning of each upkeep, if a player cast two or more spells last turn, transform Bane of Hanweir.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new TwoOrMoreSpellsCastLastTurnConditionalEffect(new TransformSelfEffect()));
    }
}
