package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.d.DeepSight;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/** Tam, Observant Sequencer // Deep Sight (SOS 237). */
@CardRegistration(set = "SOS", collectorNumber = "237")
public class TamObservantSequencerDeepSight extends Card {

    public TamObservantSequencerDeepSight() {
        setBackFaceCard(new DeepSight());

        // Landfall — Whenever a land you control enters, Tam becomes prepared.
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL), new BecomePreparedEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "DeepSight";
    }
}
