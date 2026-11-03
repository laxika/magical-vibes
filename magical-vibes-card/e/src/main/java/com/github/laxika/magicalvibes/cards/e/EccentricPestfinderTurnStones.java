package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.TurnStones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/** Eccentric Pestfinder // Turn Stones (SOC 46). */
@CardRegistration(set = "SOC", collectorNumber = "46")
@CardRegistration(set = "SOC", collectorNumber = "94")
public class EccentricPestfinderTurnStones extends Card {

    public EccentricPestfinderTurnStones() {
        setBackFaceCard(new TurnStones());

        // At the beginning of each end step, if you gained life this turn, this creature becomes prepared.
        addTrigger(Triggers.endStep(ActivePlayer.ANY),
                TriggerOptions.DEFAULT.withInterveningIf(new GainedLifeThisTurn()),
                new BecomePreparedEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "TurnStones";
    }
}
