package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.LockTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DGM", collectorNumber = "3")
public class LyevDecree extends Card {

    public LyevDecree() {
        // Detain up to two target creatures your opponents control.
        target(TargetFilters.creatureAnOpponentControls(), 0, 2)
                .addEffect(EffectSlot.SPELL, new LockTargetPermanentEffect(
                        true, true, true, EffectDuration.UNTIL_YOUR_NEXT_TURN));
    }
}
