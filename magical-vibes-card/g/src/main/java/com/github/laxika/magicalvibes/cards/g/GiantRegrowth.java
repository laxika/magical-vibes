package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardFromGraveyardToHandAndPerpetuallyBoostIfCreatureEffect;

@CardRegistration(set = "YSNC", collectorNumber = "13")
public class GiantRegrowth extends Card {

    public GiantRegrowth() {
        addEffect(EffectSlot.SPELL,
                new ReturnTargetCardFromGraveyardToHandAndPerpetuallyBoostIfCreatureEffect(3, 3));
    }
}
