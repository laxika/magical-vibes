package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockAloneEffect;

@CardRegistration(set = "8ED", collectorNumber = "206")
@CardRegistration(set = "7ED", collectorNumber = "204")
@CardRegistration(set = "USG", collectorNumber = "204")
public class Okk extends Card {

    public Okk() {
        addStaticEffect(CantAttackOrBlockAloneEffect.unlessGreaterPowerAlsoDoes());
    }
}
