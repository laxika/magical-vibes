package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "RTR", collectorNumber = "200")
@CardRegistration(set = "AKR", collectorNumber = "262")
@CardRegistration(set = "MM3", collectorNumber = "187")
@CardRegistration(set = "GK2", collectorNumber = "21")
@CardRegistration(set = "RVR", collectorNumber = "228")
@CardRegistration(set = "BRC", collectorNumber = "130")
@CardRegistration(set = "SCD", collectorNumber = "244")
public class SphinxsRevelation extends Card {

    public SphinxsRevelation() {
        addSpellEffect(new GainLifeEffect(new XValue()));
        addSpellEffect(new DrawCardEffect(new XValue()));
    }
}
