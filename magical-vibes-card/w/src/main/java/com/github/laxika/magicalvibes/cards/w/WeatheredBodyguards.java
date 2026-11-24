package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RedirectPlayerDamageToSelfEffect;

@CardRegistration(set = "TSP", collectorNumber = "46")
public class WeatheredBodyguards extends Card {

    public WeatheredBodyguards() {
        addMorph("{3}{W}");
        addStaticEffect(RedirectPlayerDamageToSelfEffect.combatDamageFromUnblockedCreatures(true));
    }
}
