package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureControllerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;

@CardRegistration(set = "M11", collectorNumber = "128")
public class ChandrasOutrage extends Card {

    public ChandrasOutrage() {
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(4));
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureControllerEffect(2));
    }
}
