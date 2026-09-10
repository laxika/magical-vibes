package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "OGW", collectorNumber = "20")
public class ImmolatingGlare extends Card {

    public ImmolatingGlare() {
        target(TargetFilters.attackingCreature())
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
    }
}
