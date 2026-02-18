package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

public class GoblinEliteInfantry extends Card {

    public GoblinEliteInfantry() {
        addEffect(EffectSlot.ON_BLOCK, new BoostSelfEffect(-1, -1));
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new BoostSelfEffect(-1, -1));
    }
}
