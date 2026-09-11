package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AddOneOfEachManaTypeProducedByLandEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleCountersOnAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToPlayerUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MultiplyTokenCreationEffect;

@CardRegistration(set = "OPC2", collectorNumber = "33")
public class SelesnyaLoftGardens extends Card {

    public SelesnyaLoftGardens() {
        addEffect(EffectSlot.STATIC, new MultiplyTokenCreationEffect(2));
        addEffect(EffectSlot.STATIC, new DoubleCountersOnAllPermanentsEffect());
        addEffect(EffectSlot.CHAOS_TRIGGERED,
                new GrantStaticEffectToPlayerUntilEndOfTurnEffect(
                        new AddOneOfEachManaTypeProducedByLandEffect(true)));
    }
}
