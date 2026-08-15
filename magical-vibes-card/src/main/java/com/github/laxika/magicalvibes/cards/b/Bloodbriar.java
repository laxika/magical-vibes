package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "EMN", collectorNumber = "151")
public class Bloodbriar extends Card {

    public Bloodbriar() {
        addEffect(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED,
                new PutCountersOnSourceEffect(1, 1, 1));
    }
}
