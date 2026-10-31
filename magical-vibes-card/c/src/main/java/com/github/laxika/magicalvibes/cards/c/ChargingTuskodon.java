package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DoubleSelfCombatDamageToPlayersEffect;

@CardRegistration(set = "RIX", collectorNumber = "97")
public class ChargingTuskodon extends Card {

    public ChargingTuskodon() {
        addStaticEffect(new DoubleSelfCombatDamageToPlayersEffect());
    }
}
