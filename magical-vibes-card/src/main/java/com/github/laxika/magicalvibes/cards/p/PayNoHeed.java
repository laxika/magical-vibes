package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenSourceEffect;

@CardRegistration(set = "M14", collectorNumber = "27")
@CardRegistration(set = "TOR", collectorNumber = "12")
public class PayNoHeed extends Card {

    public PayNoHeed() {
        addEffect(EffectSlot.SPELL, PreventDamageFromChosenSourceEffect.allDamage(null, null));
    }
}
