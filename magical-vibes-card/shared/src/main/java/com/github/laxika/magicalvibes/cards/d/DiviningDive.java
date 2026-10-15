package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SeekChosenLandOrNonlandEffect;

public class DiviningDive extends Card {

    public DiviningDive() {
        addEffect(EffectSlot.SPELL, new SeekChosenLandOrNonlandEffect(1));
    }
}
