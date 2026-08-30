package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;

@CardRegistration(set = "RIX", collectorNumber = "93")
public class Bombard extends Card {

    public Bombard() {
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(4));
    }
}
