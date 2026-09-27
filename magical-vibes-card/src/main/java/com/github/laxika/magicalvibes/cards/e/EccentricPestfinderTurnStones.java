package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.TurnStones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;

/** Eccentric Pestfinder // Turn Stones (SOC 46). */
@CardRegistration(set = "SOC", collectorNumber = "46")
@CardRegistration(set = "SOC", collectorNumber = "94")
public class EccentricPestfinderTurnStones extends Card {

    public EccentricPestfinderTurnStones() {
        setBackFaceCard(new TurnStones());

        // At the beginning of each end step, if you gained life this turn, this creature becomes prepared.
        addEffect(EffectSlot.END_STEP_TRIGGERED,
                new ConditionalEffect(new GainedLifeThisTurn(), new BecomePreparedEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "TurnStones";
    }
}
