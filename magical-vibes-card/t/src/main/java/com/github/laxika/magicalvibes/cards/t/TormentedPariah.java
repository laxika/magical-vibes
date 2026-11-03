package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.r.RampagingWerewolf;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.NoSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ISD", collectorNumber = "165")
public class TormentedPariah extends Card {

    public TormentedPariah() {
        setBackFaceCard(new RampagingWerewolf());

        // At the beginning of each upkeep, if no spells were cast last turn, transform Tormented Pariah.
        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                TriggerOptions.DEFAULT.withInterveningIf(new NoSpellsCastLastTurn()),
                new TransformSelfEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "RampagingWerewolf";
    }
}
