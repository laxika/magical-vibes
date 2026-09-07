package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndMayReturnMilledPermanentToHandEffect;

@CardRegistration(set = "MOM", collectorNumber = "204")
public class SeedOfHope extends Card {

    public SeedOfHope() {
        addEffect(EffectSlot.SPELL, new MillControllerAndMayReturnMilledPermanentToHandEffect(2));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(2));
    }
}
