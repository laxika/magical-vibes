package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenSourceEffect;

@CardRegistration(set = "M14", collectorNumber = "27")
@CardRegistration(set = "TOR", collectorNumber = "12")
@CardRegistration(set = "DDL", collectorNumber = "19")
public class PayNoHeed extends Card {

    public PayNoHeed() {
        addSpellEffect(PreventDamageFromChosenSourceEffect.allDamage(null, null));
    }
}
