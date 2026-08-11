package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "KTK", collectorNumber = "15")
public class KillShot extends Card {

    public KillShot() {
        target(TargetFilters.attackingCreature())
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
    }
}
