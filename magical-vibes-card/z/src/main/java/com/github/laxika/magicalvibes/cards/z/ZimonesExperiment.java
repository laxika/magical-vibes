package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsChooseCreaturesAndLandsEffect;

@CardRegistration(set = "SOS", collectorNumber = "169")
public class ZimonesExperiment extends Card {

    public ZimonesExperiment() {
        addEffect(EffectSlot.SPELL, new LookAtTopCardsChooseCreaturesAndLandsEffect(5));
    }
}
