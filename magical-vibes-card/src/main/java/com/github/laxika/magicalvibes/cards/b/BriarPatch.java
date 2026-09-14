package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

@CardRegistration(set = "MMQ", collectorNumber = "232")
public class BriarPatch extends Card {

    public BriarPatch() {
        addEffect(EffectSlot.ON_CREATURE_ATTACKS_YOU_DIRECTLY, new BoostTargetCreatureEffect(-1, 0));
    }
}
