package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CSP", collectorNumber = "35")
@CardRegistration(set = "SCG", collectorNumber = "36")
public class FrozenSolid extends Card {

    public FrozenSolid() {
        target(TargetFilters.creature())
                .addStaticEffect(DoesntUntapEffect.enchanted())
                .addTrigger(Triggers.enchantedCreatureDealtDamage(),
                        new DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING));
    }
}
