package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostPerCreatureCardInGraveyardEffect;

@CardRegistration(set = "DKA", collectorNumber = "115")
public class Ghoultree extends Card {

    public Ghoultree() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostPerCreatureCardInGraveyardEffect(1));
    }
}
