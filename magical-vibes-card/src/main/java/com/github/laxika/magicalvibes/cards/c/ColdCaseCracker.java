package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "MKM", collectorNumber = "46")
public class ColdCaseCracker extends Card {

    public ColdCaseCracker() {
        addEffect(EffectSlot.ON_DEATH, CreateTokenEffect.ofClueToken(1));
    }
}
