package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.OpponentMayPlayCreatureEffect;

public class HuntedWumpus extends Card {

    public HuntedWumpus() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new OpponentMayPlayCreatureEffect());
    }
}
