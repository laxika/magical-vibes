package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RNA", collectorNumber = "69")
public class ConsignToThePit extends Card {

    public ConsignToThePit() {
        // Destroy target creature. Consign to the Pit deals 2 damage to that creature's controller.
        target(TargetFilters.creature()).addSpellEffect(new DestroyTargetPermanentEffect())
                .addSpellEffect(DealDamageEffect.to(Subjects.controllerOf(Subjects.target()), 2));
    }
}
