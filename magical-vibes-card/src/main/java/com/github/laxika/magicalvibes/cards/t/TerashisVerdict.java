package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

/**
 * Terashi's Verdict — {1}{W} Instant — Arcane.
 * Destroy target attacking creature with power 3 or less.
 */
@CardRegistration(set = "BOK", collectorNumber = "27")
public class TerashisVerdict extends Card {

    public TerashisVerdict() {
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsAttackingPredicate(),
                        new PermanentPowerAtMostPredicate(3)
                )),
                "Target must be an attacking creature with power 3 or less."
        )).addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect(false));
    }
}
