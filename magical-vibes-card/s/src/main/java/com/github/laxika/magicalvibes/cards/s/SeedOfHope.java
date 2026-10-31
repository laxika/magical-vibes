package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndMayReturnMilledPermanentToHandEffect;

@CardRegistration(set = "MOM", collectorNumber = "204")
public class SeedOfHope extends Card {

    public SeedOfHope() {
        addSpellEffect(new MillControllerAndMayReturnMilledPermanentToHandEffect(2));
        addSpellEffect(new GainLifeEffect(2));
    }
}
