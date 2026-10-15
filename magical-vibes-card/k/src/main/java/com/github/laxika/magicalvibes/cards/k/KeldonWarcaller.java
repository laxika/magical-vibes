package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

/**
 * Keldon Warcaller — {1}{R} Creature — Human Warrior 2/2
 *
 * Whenever Keldon Warcaller attacks, put a lore counter on target Saga you control.
 */
@CardRegistration(set = "DOM", collectorNumber = "136")
public class KeldonWarcaller extends Card {

    public KeldonWarcaller() {
        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.SAGA),
                "Target must be a Saga you control"
        )).addEffect(EffectSlot.ON_ATTACK, new PutCounterOnTargetPermanentEffect(CounterType.LORE));
    }
}
