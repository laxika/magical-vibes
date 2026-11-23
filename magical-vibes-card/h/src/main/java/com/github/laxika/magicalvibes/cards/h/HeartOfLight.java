package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToRecipientsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHostOfSourceAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "10E", collectorNumber = "19")
@CardRegistration(set = "BOK", collectorNumber = "6")
public class HeartOfLight extends Card {

    public HeartOfLight() {
        target(TargetFilters.creature())
                .addStaticEffect(PreventDamageToRecipientsEffect.to(Subjects.attachedCreature()))
                .addStaticEffect(PreventDamageToRecipientsEffect.dealtBy(new PermanentIsHostOfSourceAuraPredicate()));
    }
}
