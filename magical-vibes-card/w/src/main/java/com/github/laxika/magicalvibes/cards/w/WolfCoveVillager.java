package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

@CardRegistration(set = "TLE", collectorNumber = "221")
@CardRegistration(set = "TLE", collectorNumber = "276")
public class WolfCoveVillager extends Card {

    public WolfCoveVillager() {
        // This creature enters tapped.
        addStaticEffect(new EntersTappedEffect());
    }
}
