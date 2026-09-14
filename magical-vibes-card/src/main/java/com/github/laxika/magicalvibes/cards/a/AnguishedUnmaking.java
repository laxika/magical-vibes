package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SOI", collectorNumber = "242")
@CardRegistration(set = "SIR", collectorNumber = "229")
@CardRegistration(set = "2X2", collectorNumber = "170")
public class AnguishedUnmaking extends Card {

    public AnguishedUnmaking() {
        target(TargetFilters.nonlandPermanent())
                .addEffect(EffectSlot.SPELL, new ExileTargetPermanentEffect())
                .addEffect(EffectSlot.SPELL, new LoseLifeEffect(3));
    }
}
