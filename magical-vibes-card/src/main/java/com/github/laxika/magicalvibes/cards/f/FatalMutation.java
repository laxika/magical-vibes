package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHostOfSourceAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SCG", collectorNumber = "66")
public class FatalMutation extends Card {

    public FatalMutation() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_SELF_OR_ANY_PERMANENT_TURNS_FACE_UP,
                        new TriggeringPermanentConditionalEffect(
                                new PermanentIsHostOfSourceAuraPredicate(),
                                new DestroyReferencedPermanentEffect(PermanentReference.ATTACHED, true)));
    }
}
