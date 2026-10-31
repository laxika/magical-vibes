package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

@CardRegistration(set = "SPM", collectorNumber = "62")
@CardRegistration(set = "OM1", collectorNumber = "52")
public class RiskyResearch extends Card {

    public RiskyResearch() {
        addSpellEffect(new SurveilEffect(2));
        addSpellEffect(new DrawCardEffect(2));
        addSpellEffect(new LoseLifeEffect(2));
    }
}
