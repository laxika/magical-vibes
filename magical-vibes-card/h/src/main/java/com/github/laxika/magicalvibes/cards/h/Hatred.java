package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;

@CardRegistration(set = "EXO", collectorNumber = "64")
public class Hatred extends Card {

    public Hatred() {
        addCastingCost(PayLifeCost.x());
        addSpellEffect(new BoostTargetCreatureEffect(new XValue(), new Fixed(0)));
    }
}
