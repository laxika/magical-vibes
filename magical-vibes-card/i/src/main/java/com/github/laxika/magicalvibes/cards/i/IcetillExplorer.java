package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.PlayLandsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.PlaysAdditionalLandEachTurnEffect;

@CardRegistration(set = "EOE", collectorNumber = "192")
public class IcetillExplorer extends Card {

    public IcetillExplorer() {
        addEffect(EffectSlot.STATIC, new PlaysAdditionalLandEachTurnEffect(1));
        addEffect(EffectSlot.STATIC, new PlayLandsFromGraveyardEffect());
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new MillEffect(1, MillRecipient.CONTROLLER));
    }
}
