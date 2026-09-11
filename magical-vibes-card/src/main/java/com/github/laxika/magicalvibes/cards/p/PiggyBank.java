package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "DSK", collectorNumber = "148")
public class PiggyBank extends Card {

    public PiggyBank() {
        addEffect(EffectSlot.ON_DEATH, CreateTokenEffect.ofTreasureToken(1));
    }
}
