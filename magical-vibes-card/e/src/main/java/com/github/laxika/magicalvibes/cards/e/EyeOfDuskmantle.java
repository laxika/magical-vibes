package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CastSurveilledCardsFromGraveyardByPayingLifeEffect;

@CardRegistration(set = "MKC", collectorNumber = "27")
@CardRegistration(set = "MKC", collectorNumber = "337")
public class EyeOfDuskmantle extends Card {

    public EyeOfDuskmantle() {
        addEffect(EffectSlot.STATIC, new CastSurveilledCardsFromGraveyardByPayingLifeEffect());
    }
}
