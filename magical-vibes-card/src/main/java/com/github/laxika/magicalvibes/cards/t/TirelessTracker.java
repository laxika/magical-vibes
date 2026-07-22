package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "INR", collectorNumber = "219")
public class TirelessTracker extends Card {

    public TirelessTracker() {
        // Landfall — Whenever a land you control enters, investigate.
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD, CreateTokenEffect.ofClueToken(1));

        // Whenever you sacrifice a Clue, put a +1/+1 counter on this creature.
        addEffect(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED,
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.CLUE),
                        new PutCountersOnSourceEffect(1, 1, 1)
                ));
    }
}
