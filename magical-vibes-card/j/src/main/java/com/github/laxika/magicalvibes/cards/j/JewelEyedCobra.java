package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "MH2", collectorNumber = "168")
public class JewelEyedCobra extends Card {

    public JewelEyedCobra() {
        addEffect(EffectSlot.ON_DEATH, CreateTokenEffect.ofTreasureToken(1));
    }
}
