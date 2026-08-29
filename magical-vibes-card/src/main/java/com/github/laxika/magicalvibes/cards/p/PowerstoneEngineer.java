package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "BRO", collectorNumber = "20")
public class PowerstoneEngineer extends Card {

    public PowerstoneEngineer() {
        addEffect(EffectSlot.ON_DEATH, CreateTokenEffect.ofPowerstoneToken(new Fixed(1)));
    }
}
