package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MKM", collectorNumber = "28")
@CardRegistration(set = "MKM", collectorNumber = "291")
public class NotOnMyWatch extends Card {

    public NotOnMyWatch() {
        target(TargetFilters.attackingCreature())
                .addEffect(EffectSlot.SPELL, new ExileTargetPermanentEffect());
    }
}
