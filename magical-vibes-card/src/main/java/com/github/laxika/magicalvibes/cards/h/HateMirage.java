package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "C19", collectorNumber = "26")
public class HateMirage extends Card {

    public HateMirage() {
        // Choose up to two target creatures you don't control. For each of those creatures,
        // create a token that's a copy of that creature. Those tokens gain haste. Exile them
        // at the beginning of the next end step.
        target(TargetFilters.creatureAnOpponentControls(), 0, 2)
                .addEffect(EffectSlot.SPELL, new CreateTokenCopyOfTargetPermanentEffect(true, true));
    }
}
