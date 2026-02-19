package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageOnLandTapEffect;

public class Manabarbs extends Card {

    public Manabarbs() {
        addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND, new DealDamageOnLandTapEffect(1));
    }
}
