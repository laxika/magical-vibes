package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AttachedCreatureDoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "XLN", collectorNumber = "281")
public class CastawaysDespair extends Card {

    public CastawaysDespair() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ))
                // When this Aura enters, tap enchanted creature.
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new TapTargetPermanentEffect())
                // Enchanted creature doesn't untap during its controller's untap step.
                .addEffect(EffectSlot.STATIC, new AttachedCreatureDoesntUntapEffect());
    }
}
