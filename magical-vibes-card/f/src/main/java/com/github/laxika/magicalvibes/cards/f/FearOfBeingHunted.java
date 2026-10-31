package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedIfAbleEffect;

@CardRegistration(set = "DSK", collectorNumber = "134")
public class FearOfBeingHunted extends Card {

    public FearOfBeingHunted() {
        addStaticEffect(new MustBeBlockedIfAbleEffect());
    }
}
