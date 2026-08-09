package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "UDS", collectorNumber = "86")
public class GoblinMasons extends Card {

    public GoblinMasons() {
        target(new PermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.WALL),
                "Target must be a Wall"
        )).addEffect(EffectSlot.ON_DEATH, new DestroyTargetPermanentEffect());
    }
}
