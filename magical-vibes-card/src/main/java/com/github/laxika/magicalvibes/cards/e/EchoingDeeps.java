package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyLandFromGraveyardOnEnterEffect;

@CardRegistration(set = "LCI", collectorNumber = "271")
@CardRegistration(set = "LCI", collectorNumber = "346")
public class EchoingDeeps extends Card {

    public EchoingDeeps() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CopyLandFromGraveyardOnEnterEffect());
    }
}
