package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.LockTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


@CardRegistration(set = "RTR", collectorNumber = "6")
@CardRegistration(set = "GK2", collectorNumber = "3")
@CardRegistration(set = "RVR", collectorNumber = "6")
public class AzoriusJusticiar extends Card {

    public AzoriusJusticiar() {
        // When this creature enters, detain up to two target creatures your opponents control.
        target(TargetFilters.creatureAnOpponentControls(), 0, 2)
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new LockTargetPermanentEffect(
                        true, true, true, EffectDuration.UNTIL_YOUR_NEXT_TURN));
    }
}
