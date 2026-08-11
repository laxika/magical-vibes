package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureAndAllWithSameNameEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DST", collectorNumber = "41")
public class EchoingDecay extends Card {

    public EchoingDecay() {
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL,
                new BoostTargetCreatureAndAllWithSameNameEffect(-2, -2));
    }
}
