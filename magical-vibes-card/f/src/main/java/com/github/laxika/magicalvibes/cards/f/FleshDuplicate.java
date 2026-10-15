package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "WHO", collectorNumber = "44")
@CardRegistration(set = "WHO", collectorNumber = "359")
@CardRegistration(set = "WHO", collectorNumber = "649")
@CardRegistration(set = "WHO", collectorNumber = "950")
public class FleshDuplicate extends Card {

    public FleshDuplicate() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                CopyPermanentOnEnterEffect.withVanishingIfCopiedPermanentLacksIt(
                        new PermanentIsCreaturePredicate(), "creature"));
    }
}
