package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "DSK", collectorNumber = "123")
public class WintersIntervention extends Card {

    public WintersIntervention() {
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(2));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(2));
    }
}
