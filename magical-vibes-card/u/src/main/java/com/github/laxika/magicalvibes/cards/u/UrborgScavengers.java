package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardPutCounterOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GainKeywordsOfCardsExiledWithSourceEffect;

@CardRegistration(set = "MAT", collectorNumber = "15")
@CardRegistration(set = "MAT", collectorNumber = "65")
@CardRegistration(set = "MAT", collectorNumber = "115")
@CardRegistration(set = "MAT", collectorNumber = "158")
@CardRegistration(set = "MAT", collectorNumber = "199")
public class UrborgScavengers extends Card {

    public UrborgScavengers() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileTargetCardFromGraveyardPutCounterOnSourceEffect());
        addEffect(EffectSlot.ON_ATTACK,
                new ExileTargetCardFromGraveyardPutCounterOnSourceEffect());
        addEffect(EffectSlot.STATIC, new GainKeywordsOfCardsExiledWithSourceEffect());
    }
}
