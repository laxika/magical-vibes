package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardAnyOpponentMayGraveyardOrDealDamageEffect;

@CardRegistration(set = "SOI", collectorNumber = "181")
public class SinProdder extends Card {

    public SinProdder() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new RevealTopCardAnyOpponentMayGraveyardOrDealDamageEffect());
    }
}
