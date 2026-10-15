package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "LCI", collectorNumber = "211")
@CardRegistration(set = "LCI", collectorNumber = "383")
public class SentinelOfTheNamelessCity extends Card {

    public SentinelOfTheNamelessCity() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.ofMapToken(1));
        addEffect(EffectSlot.ON_ATTACK, CreateTokenEffect.ofMapToken(1));
    }
}
