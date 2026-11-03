package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.NoSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ISD", collectorNumber = "208")
public class UlvenwaldMystics extends Card {

    public UlvenwaldMystics() {
        setBackFaceCard(new UlvenwaldPrimordials());

        // At the beginning of each upkeep, if no spells were cast last turn, transform Ulvenwald Mystics.
        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                TriggerOptions.DEFAULT.withInterveningIf(new NoSpellsCastLastTurn()),
                new TransformSelfEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "UlvenwaldPrimordials";
    }
}
