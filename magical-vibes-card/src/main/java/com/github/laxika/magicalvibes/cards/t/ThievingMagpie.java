package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

public class ThievingMagpie extends Card {

    public ThievingMagpie() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new DrawCardEffect());
    }
}
