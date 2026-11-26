package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.BeholdCost;

@CardRegistration(set = "TDM", collectorNumber = "74")
public class CausticExhale extends Card {

    public CausticExhale() {
        addCastingCost(BeholdCost.orPayMana(CardSubtype.DRAGON, "{1}"));
        addSpellEffect(BoostEffect.target(-3, -3));
    }
}
