package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSupertypeToEnchantedPermanentEffect;

@CardRegistration(set = "DOM", collectorNumber = "54")
public class InBolassClutches extends Card {

    public InBolassClutches() {
        addStaticEffect(new ControlEnchantedCreatureEffect());
        addStaticEffect(new GrantSupertypeToEnchantedPermanentEffect(CardSupertype.LEGENDARY));
    }
}
