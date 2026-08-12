package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "KTK", collectorNumber = "210")
public class UtterEnd extends Card {

    public UtterEnd() {
        target(TargetFilters.nonlandPermanent())
                .addEffect(EffectSlot.SPELL, new ExileTargetPermanentEffect());
    }
}
