package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;

@CardRegistration(set = "VOW", collectorNumber = "158")
public class FlameBlessedBolt extends Card {

    public FlameBlessedBolt() {
        addEffect(EffectSlot.SPELL,
                new DealDamageToTargetCreatureOrPlaneswalkerEffect(2, null, true));
    }
}
