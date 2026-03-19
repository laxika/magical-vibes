package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SubtypeConditionalEffect;

@CardRegistration(set = "ISD", collectorNumber = "125")
public class VillageCannibals extends Card {

    public VillageCannibals() {
        // Whenever another Human creature dies, put a +1/+1 counter on Village Cannibals.
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES,
                new SubtypeConditionalEffect(CardSubtype.HUMAN, new PutCountersOnSourceEffect(1, 1, 1)));
    }
}
