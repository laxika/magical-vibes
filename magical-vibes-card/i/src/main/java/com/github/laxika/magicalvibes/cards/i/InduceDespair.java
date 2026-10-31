package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardFromHandCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "ROE", collectorNumber = "114")
@CardRegistration(set = "DDP", collectorNumber = "53")
public class InduceDespair extends Card {

    public InduceDespair() {
        addCastingCost(new RevealCardFromHandCost(new CardTypePredicate(CardType.CREATURE), "creature", true));
        addSpellEffect(new BoostTargetCreatureEffect(new Scaled(new XValue(), -1), new Scaled(new XValue(), -1)));
    }
}
