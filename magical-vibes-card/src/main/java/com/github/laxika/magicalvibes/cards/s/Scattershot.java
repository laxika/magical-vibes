package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.StormEffect;

@CardRegistration(set = "SCG", collectorNumber = "102")
public class Scattershot extends Card {

    public Scattershot() {
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(1));
        addEffect(EffectSlot.ON_SELF_CAST, new StormEffect());
    }
}
