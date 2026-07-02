package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetOpponentAndUpToCreaturesThatPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.condition.TwoOrMoreSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;

public class RavagerOfTheFells extends Card {

    public RavagerOfTheFells() {
        // Whenever this creature transforms into Ravager of the Fells, it deals 2 damage
        // to target opponent and 2 damage to up to one target creature that player controls.
        addEffect(EffectSlot.ON_TRANSFORM_TO_BACK_FACE,
                new DealDamageToTargetOpponentAndUpToCreaturesThatPlayerControlsEffect(2, 2, 1));

        // At the beginning of each upkeep, if a player cast two or more spells last turn,
        // transform Ravager of the Fells.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new ConditionalEffect(new TwoOrMoreSpellsCastLastTurn(), new TransformSelfEffect()));
    }
}
