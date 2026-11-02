package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "CON", collectorNumber = "41")
public class CorruptedRoots extends Card {

    public CorruptedRoots() {
        target(new PermanentPredicateTargetFilter(
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.FOREST, CardSubtype.PLAINS)),
                "Target must be a Forest or Plains"
        ));
        // Whenever enchanted land becomes tapped, its controller loses 2 life.
        addTrigger(Triggers.enchantedPermanentBecomesTapped(),
                new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PERMANENT_CONTROLLER));
    }
}
