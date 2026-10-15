package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;

@CardRegistration(set = "GRN", collectorNumber = "16")
public class InspiringUnicorn extends Card {

    public InspiringUnicorn() {
        addEffect(EffectSlot.ON_ATTACK, new BoostAllOwnCreaturesEffect(1, 1));
    }
}
