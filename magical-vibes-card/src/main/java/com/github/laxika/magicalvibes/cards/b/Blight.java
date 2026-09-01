package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "6ED", collectorNumber = "113")
@CardRegistration(set = "5ED", collectorNumber = "144")
@CardRegistration(set = "4ED", collectorNumber = "122")
@CardRegistration(set = "LEG", collectorNumber = "89")
public class Blight extends Card {

    public Blight() {
        target(TargetFilters.land());
        // When enchanted land becomes tapped, destroy it.
        addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_TAPPED, new DestroyReferencedPermanentEffect(PermanentReference.ATTACHED));
    }
}
