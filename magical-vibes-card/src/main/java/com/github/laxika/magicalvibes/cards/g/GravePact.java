package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachOpponentSacrificesCreatureEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "144")
public class GravePact extends Card {

    public GravePact() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new EachOpponentSacrificesCreatureEffect());
    }
}
