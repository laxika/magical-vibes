package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileCreaturesDamagedByControlledSourceInsteadOfDyingEffect;

public class EtchingOfKumano extends Card {

    public EtchingOfKumano() {
        addEffect(EffectSlot.STATIC, new ExileCreaturesDamagedByControlledSourceInsteadOfDyingEffect());
    }
}
