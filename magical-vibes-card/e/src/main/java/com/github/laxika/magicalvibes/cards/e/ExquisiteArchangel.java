package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReplaceControllerLossWithExileAndStartingLifeEffect;

@CardRegistration(set = "AER", collectorNumber = "18")
public class ExquisiteArchangel extends Card {

    public ExquisiteArchangel() {
        addEffect(EffectSlot.STATIC, new ReplaceControllerLossWithExileAndStartingLifeEffect());
    }
}
