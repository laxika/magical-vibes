package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

@CardRegistration(set = "EMN", collectorNumber = "52")
public class ContingencyPlan extends Card {

    public ContingencyPlan() {
        addSpellEffect(new SurveilEffect(5));
    }
}
