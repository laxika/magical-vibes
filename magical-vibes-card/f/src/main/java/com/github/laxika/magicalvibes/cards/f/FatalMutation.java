package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHostOfSourceAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SCG", collectorNumber = "66")
public class FatalMutation extends Card {

    public FatalMutation() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.turnedFaceUp(new PermanentIsHostOfSourceAuraPredicate(), ObjectRelation.ANY),
                        new DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING, true));
    }
}
