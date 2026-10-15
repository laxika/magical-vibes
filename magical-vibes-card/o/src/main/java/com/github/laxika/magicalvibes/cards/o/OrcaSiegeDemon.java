package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DivisionMode;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "DMC", collectorNumber = "37")
@CardRegistration(set = "DMC", collectorNumber = "59")
public class OrcaSiegeDemon extends Card {

    public OrcaSiegeDemon() {
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES,
                new PutCountersOnSourceEffect(1, 1, 1));

        // The death trigger's power is carried as the death-event value; the divided damage
        // assignments are announced through the existing death-trigger assignment path.
        addEffect(EffectSlot.ON_DEATH,
                new DealDividedDamageEffect(new EventValue(), null, DivisionMode.CHOSEN,
                        null, 0, false, false, true));
    }
}
