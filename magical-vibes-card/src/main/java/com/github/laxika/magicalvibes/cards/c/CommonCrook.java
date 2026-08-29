package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "SPM", collectorNumber = "53")
public class CommonCrook extends Card {

    public CommonCrook() {
        addEffect(EffectSlot.ON_DEATH, CreateTokenEffect.ofTreasureToken(1));
    }
}
