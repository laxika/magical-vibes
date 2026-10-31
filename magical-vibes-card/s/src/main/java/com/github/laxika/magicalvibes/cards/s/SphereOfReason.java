package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.PreventFixedDamagePerSourceToControllerEffect;

import java.util.Set;

@CardRegistration(set = "ODY", collectorNumber = "51")
public class SphereOfReason extends Card {

    public SphereOfReason() {
        addStaticEffect(PreventFixedDamagePerSourceToControllerEffect.fromColors(Set.of(CardColor.BLUE), 2));
    }
}
