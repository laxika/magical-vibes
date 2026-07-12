package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "SHM", collectorNumber = "106")
public class SlinkingGiant extends Card {

    public SlinkingGiant() {
        // Whenever this creature blocks or becomes blocked, it gets -3/-0 until end of turn.
        addEffect(EffectSlot.ON_BLOCK, new BoostSelfEffect(-3, 0));
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new BoostSelfEffect(-3, 0));
    }
}
