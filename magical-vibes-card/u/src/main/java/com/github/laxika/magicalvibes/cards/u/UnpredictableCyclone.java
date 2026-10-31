package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.UnpredictableCycloneDrawReplacementEffect;

@CardRegistration(set = "IKO", collectorNumber = "139")
public class UnpredictableCyclone extends Card {

    public UnpredictableCyclone() {
        addStaticEffect(new UnpredictableCycloneDrawReplacementEffect());
        addCycling("{2}");
    }
}
