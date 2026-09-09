package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.d.DeepSight;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;

/** Tam, Observant Sequencer // Deep Sight (SOS 237). */
@CardRegistration(set = "SOS", collectorNumber = "237")
public class TamObservantSequencerDeepSight extends Card {

    public TamObservantSequencerDeepSight() {
        setBackFaceCard(new DeepSight());

        // Landfall — Whenever a land you control enters, Tam becomes prepared.
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD, new BecomePreparedEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "DeepSight";
    }
}
