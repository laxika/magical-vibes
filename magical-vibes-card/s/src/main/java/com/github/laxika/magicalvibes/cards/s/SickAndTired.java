package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ULG", collectorNumber = "66")
public class SickAndTired extends Card {

    public SickAndTired() {
        // Two target creatures each get -1/-1 until end of turn.
        target(TargetFilters.creature(), 2, 2)
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(-1, -1));
    }
}
