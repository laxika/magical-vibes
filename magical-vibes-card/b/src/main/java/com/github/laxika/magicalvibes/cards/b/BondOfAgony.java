package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PayXLifeCost;

@CardRegistration(set = "DIS", collectorNumber = "38")
public class BondOfAgony extends Card {

    public BondOfAgony() {
        addCastingCost(new PayXLifeCost());
        addSpellEffect(new LoseLifeEffect(new XValue(), LoseLifeRecipient.EACH_OPPONENT));
    }
}
