package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

/**
 * Crimson Manticore — {2}{R}{R} Creature — Manticore (2/2).
 * Flying (auto-loaded from Scryfall).
 * {R}, {T}: This creature deals 1 damage to target attacking or blocking creature.
 */
@CardRegistration(set = "5ED", collectorNumber = "217")
@CardRegistration(set = "4ED", collectorNumber = "183")
public class CrimsonManticore extends Card {

    public CrimsonManticore() {
        addActivatedAbility(new ActivatedAbility(
                true,  // requiresTap
                "{R}",
                List.of(new DealDamageToTargetCreatureEffect(1)),
                "{R}, {T}: Crimson Manticore deals 1 damage to target attacking or blocking creature.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsAttackingPredicate(),
                                        new PermanentIsBlockingPredicate()
                                ))
                        )),
                        "Target must be an attacking or blocking creature."
                )
        ));
    }
}
