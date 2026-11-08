package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOM", collectorNumber = "32")
public class DissipationField extends Card {

    public DissipationField() {
        addTrigger(Triggers.permanentDealsDamageToYou(), ReturnToHandEffect.triggering());
    }
}
