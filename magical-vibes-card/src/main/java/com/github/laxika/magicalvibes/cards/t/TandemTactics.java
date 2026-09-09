package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "BFZ", collectorNumber = "52")
public class TandemTactics extends Card {

    public TandemTactics() {
        // Up to two target creatures each get +1/+2 until end of turn. You gain 2 life.
        target(TargetFilters.creature(), 0, 2)
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(1, 2))
                .addEffect(EffectSlot.SPELL, new GainLifeEffect(2));
    }
}
