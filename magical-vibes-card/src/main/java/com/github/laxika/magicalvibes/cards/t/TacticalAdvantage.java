package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

/**
 * Tactical Advantage — {W} Instant.
 * Target blocking or blocked creature you control gets +2/+2 until end of turn.
 */
@CardRegistration(set = "OANA", collectorNumber = "12")
public class TacticalAdvantage extends Card {

    public TacticalAdvantage() {
        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsBlockingPredicate(),
                                new PermanentIsBlockedPredicate()
                        ))
                )),
                "Target must be a blocking or blocked creature you control"
        )).addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(2, 2));
    }
}
