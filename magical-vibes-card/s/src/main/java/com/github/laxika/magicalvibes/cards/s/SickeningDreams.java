package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DiscardXCardsCost;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;

@CardRegistration(set = "TOR", collectorNumber = "83")
@CardRegistration(set = "PD3", collectorNumber = "18")
public class SickeningDreams extends Card {

    public SickeningDreams() {
        addCastingCost(new DiscardXCardsCost());
        addSpellEffect(new MassDamageEffect(new XValue(), true));
    }
}
