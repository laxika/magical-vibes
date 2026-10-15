package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "34")
@CardRegistration(set = "RVR", collectorNumber = "63")
@CardRegistration(set = "AA4", collectorNumber = "10")
public class TidespoutTyrant extends Card {

    public TidespoutTyrant() {
        // Whenever you cast a spell, return target permanent to its owner's hand.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(null, List.of(ReturnToHandEffect.target()), null,
                        TargetFilters.permanent()));
    }
}
