package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;


@CardRegistration(set = "AKH", collectorNumber = "164")
@CardRegistration(set = "AKR", collectorNumber = "189")
public class DissentersDeliverance extends Card {

    public DissentersDeliverance() {
        // Destroy target artifact.
        target(TargetFilters.artifact()).addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());

        // Cycling {G} ({G}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addCycling("{G}");
    }
}
