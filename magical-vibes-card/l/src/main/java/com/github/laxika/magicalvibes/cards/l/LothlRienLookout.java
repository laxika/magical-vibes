package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "HOC", collectorNumber = "209")
public class LothlRienLookout extends Card {

    public LothlRienLookout() {
        addEffect(EffectSlot.ON_ATTACK, new ScryEffect(1));
    }
}
