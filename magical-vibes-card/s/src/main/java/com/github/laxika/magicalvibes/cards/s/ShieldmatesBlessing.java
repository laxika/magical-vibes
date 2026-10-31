package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

@CardRegistration(set = "ZEN", collectorNumber = "35")
public class ShieldmatesBlessing extends Card {

    public ShieldmatesBlessing() {
        addSpellEffect(PreventDamageEffect.nextToTarget(3));
    }
}
