package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAnyNumberOfPermanentsToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "SOK", collectorNumber = "22")
public class PlowThroughReito extends Card {

    public PlowThroughReito() {
        // Sweep — Return any number of Plains you control to their owner's hand.
        addEffect(EffectSlot.SPELL,
                new ReturnAnyNumberOfPermanentsToHandEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.PLAINS)));

        // Target creature gets +1/+1 until end of turn for each Plains returned this way.
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(new EventValue(), new EventValue()));
    }
}
