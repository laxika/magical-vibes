package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.NthSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

import java.util.List;

@CardRegistration(set = "EOE", collectorNumber = "61")
public class IllvoiOperative extends Card {

    public IllvoiOperative() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new NthSpellCastTriggerEffect(
                2,
                List.of(new PutCountersOnSourceEffect(1, 1, 1))
        ));
    }
}
