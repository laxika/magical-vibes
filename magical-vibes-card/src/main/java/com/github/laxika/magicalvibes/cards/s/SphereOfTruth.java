package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventFixedDamagePerSourceToControllerEffect;

import java.util.Set;

@CardRegistration(set = "ODY", collectorNumber = "52")
public class SphereOfTruth extends Card {

    public SphereOfTruth() {
        addEffect(EffectSlot.STATIC,
                PreventFixedDamagePerSourceToControllerEffect.fromColors(Set.of(CardColor.WHITE), 2));
    }
}
