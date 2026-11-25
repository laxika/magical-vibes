package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;

@CardRegistration(set = "NPH", collectorNumber = "100")
@CardRegistration(set = "ONE", collectorNumber = "155")
@CardRegistration(set = "DDL", collectorNumber = "68")
public class VoltCharge extends Card {

    public VoltCharge() {
        addSpellEffect(DealDamageEffect.toAnyTarget(3));
        addSpellEffect(new ProliferateEffect());
    }
}
