package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "XLN", collectorNumber = "281")
public class CastawaysDespair extends Card {

    public CastawaysDespair() {
        target(TargetFilters.creature())
                // Enchanted creature doesn't untap during its controller's untap step.
                .addStaticEffect(DoesntUntapEffect.enchanted());
        // When this Aura enters, tap enchanted creature (the trigger doesn't target).
        addTrigger(Triggers.enters(ObjectRelation.THIS), new TapPermanentsEffect(TapUntapScope.ENCHANTED));
    }
}
