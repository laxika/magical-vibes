package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

@CardRegistration(set = "ORI", collectorNumber = "119")
public class ShamblingGhoul extends Card {

    public ShamblingGhoul() {
        // Shambling Ghoul enters the battlefield tapped.
        addStaticEffect(new EntersTappedEffect());
    }
}
