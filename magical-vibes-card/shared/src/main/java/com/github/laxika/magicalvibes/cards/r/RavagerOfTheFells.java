package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetAndUpToCreaturesThatPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.condition.TwoOrMoreSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class RavagerOfTheFells extends Card {

    public RavagerOfTheFells() {
        // Whenever this creature transforms into Ravager of the Fells, it deals 2 damage
        // to target opponent and 2 damage to up to one target creature that player controls.
        addTrigger(Triggers.transformsToBackFace(ObjectRelation.THIS),
                new DealDamageToTargetAndUpToCreaturesThatPlayerControlsEffect(2, 2, 1, false, false));

        // At the beginning of each upkeep, if a player cast two or more spells last turn,
        // transform Ravager of the Fells.
        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                TriggerOptions.DEFAULT.withInterveningIf(new TwoOrMoreSpellsCastLastTurn()),
                new TransformSelfEffect());
    }
}
