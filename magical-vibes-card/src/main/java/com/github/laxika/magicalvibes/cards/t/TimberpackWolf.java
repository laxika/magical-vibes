package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostByOtherCreaturesWithSameNameEffect;

@CardRegistration(set = "M13", collectorNumber = "194")
@CardRegistration(set = "ORI", collectorNumber = "200")
public class TimberpackWolf extends Card {

    public TimberpackWolf() {
        addEffect(EffectSlot.STATIC, new BoostByOtherCreaturesWithSameNameEffect(1, 1, true));
    }
}
