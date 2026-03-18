package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.UnattachEquipmentFromTargetPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "SOM", collectorNumber = "7")
public class FulgentDistraction extends Card {

    public FulgentDistraction() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ), 2, 2)
                .addEffect(EffectSlot.SPELL, new TapTargetPermanentEffect())
                .addEffect(EffectSlot.SPELL, new UnattachEquipmentFromTargetPermanentsEffect());
    }
}
