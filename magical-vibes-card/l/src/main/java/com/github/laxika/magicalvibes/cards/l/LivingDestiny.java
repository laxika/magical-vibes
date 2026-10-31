package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardFromHandCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "ROE", collectorNumber = "195")
public class LivingDestiny extends Card {

    public LivingDestiny() {
        addCastingCost(new RevealCardFromHandCost(new CardTypePredicate(CardType.CREATURE), "creature", true));
        addSpellEffect(new GainLifeEffect(new XValue()));
    }
}
