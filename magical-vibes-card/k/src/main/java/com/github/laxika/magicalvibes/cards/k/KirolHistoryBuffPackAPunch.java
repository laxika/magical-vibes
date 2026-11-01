package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.p.PackAPunch;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/**
 * Kirol, History Buff // Pack a Punch (SOS 198).
 *
 * <p>Kirol becomes prepared whenever one or more cards leave its controller's graveyard.
 */
@CardRegistration(set = "SOS", collectorNumber = "198")
public class KirolHistoryBuffPackAPunch extends Card {

    public KirolHistoryBuffPackAPunch() {
        setBackFaceCard(new PackAPunch());

        // Whenever one or more cards leave your graveyard, Kirol becomes prepared.
        addTrigger(new TriggerCondition(Triggers.leavesGraveyard(null, ObjectRelation.YOU_OWN)),
                TriggerOptions.ONE_OR_MORE, new BecomePreparedEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "PackAPunch";
    }
}
