package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostBySharedCreatureTypeEffect;

public class CoatOfArms extends Card {

    public CoatOfArms() {
        addEffect(EffectSlot.STATIC, new BoostBySharedCreatureTypeEffect());
    }
}
