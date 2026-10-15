package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventFixedDamagePerSourceToControllerEffect;

@CardRegistration(set = "MRD", collectorNumber = "26")
public class SphereOfPurity extends Card {

    public SphereOfPurity() {
        // "If an artifact would deal damage to you, prevent 1 of that damage."
        addEffect(EffectSlot.STATIC, PreventFixedDamagePerSourceToControllerEffect.fromArtifacts(1));
    }
}
