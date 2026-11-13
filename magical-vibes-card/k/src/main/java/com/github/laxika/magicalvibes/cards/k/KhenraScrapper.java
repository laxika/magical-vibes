package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExertAsItAttacksEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOU", collectorNumber = "100")
@CardRegistration(set = "AKR", collectorNumber = "163")
public class KhenraScrapper extends Card {

    public KhenraScrapper() {
        addStaticEffect(new ExertAsItAttacksEffect());
        addTrigger(Triggers.exertedAsItAttacks(), new BoostSelfEffect(2, 0));
    }
}
