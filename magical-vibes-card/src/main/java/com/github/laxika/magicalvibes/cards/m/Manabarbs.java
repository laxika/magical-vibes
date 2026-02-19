package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageOnLandTapEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "218")
public class Manabarbs extends Card {

    public Manabarbs() {
        addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND, new DealDamageOnLandTapEffect(1));
    }
}
