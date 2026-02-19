package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostBySharedCreatureTypeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "316")
public class CoatOfArms extends Card {

    public CoatOfArms() {
        addEffect(EffectSlot.STATIC, new BoostBySharedCreatureTypeEffect());
    }
}
