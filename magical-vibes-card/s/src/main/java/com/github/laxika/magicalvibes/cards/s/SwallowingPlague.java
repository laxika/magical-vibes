package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "CHK", collectorNumber = "146")
public class SwallowingPlague extends Card {

    public SwallowingPlague() {
        // Swallowing Plague deals X damage to target creature and you gain X life.
        // The life gain is the chosen X, not the damage actually dealt, so both
        // effects read the spell's X independently.
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(new XValue()));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(new XValue()));
    }
}
