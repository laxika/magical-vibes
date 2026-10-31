package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

@CardRegistration(set = "VOW", collectorNumber = "135")
@CardRegistration(set = "DBL", collectorNumber = "402")
public class UnhallowedPhalanx extends Card {

    public UnhallowedPhalanx() {
        // This creature enters tapped.
        addStaticEffect(new EntersTappedEffect());
    }
}
