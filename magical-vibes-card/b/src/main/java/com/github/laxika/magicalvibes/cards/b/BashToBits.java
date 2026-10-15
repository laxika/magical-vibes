package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ODY", collectorNumber = "176")
public class BashToBits extends Card {

    public BashToBits() {
        target(TargetFilters.artifact())
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
        addCastingOption(new FlashbackCast("{4}{R}{R}"));
    }
}
