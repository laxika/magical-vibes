package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "OGW", collectorNumber = "75")
public class OblivionStrike extends Card {

    public OblivionStrike() {
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, new ExileTargetPermanentEffect());
    }
}
