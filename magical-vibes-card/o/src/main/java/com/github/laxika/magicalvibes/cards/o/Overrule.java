package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "DIS", collectorNumber = "120")
@CardRegistration(set = "DDI", collectorNumber = "32")
public class Overrule extends Card {

    public Overrule() {
        addSpellEffect(UnlessPaysEffect.counterUnlessPaysX());
        addSpellEffect(new GainLifeEffect(new XValue()));
    }
}
