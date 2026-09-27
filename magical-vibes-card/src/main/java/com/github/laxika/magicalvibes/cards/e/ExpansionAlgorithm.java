package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;

@CardRegistration(set = "SOC", collectorNumber = "19")
@CardRegistration(set = "SOC", collectorNumber = "69")
public class ExpansionAlgorithm extends Card {

    public ExpansionAlgorithm() {
        addEffect(EffectSlot.SPELL, new ProliferateEffect(new XValue()));
    }
}
