package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;

import java.util.List;

/**
 * Kithkin Shielddare — {1}{W} Creature — Kithkin Soldier (1/1).
 * {W}, {T}: Target blocking creature gets +2/+2 until end of turn.
 */
@CardRegistration(set = "SHM", collectorNumber = "10")
public class KithkinShielddare extends Card {

    public KithkinShielddare() {
        addActivatedAbility(new ActivatedAbility(true, "{W}", List.of(new BoostTargetCreatureEffect(2, 2)),
                "{W}, {T}: Target blocking creature gets +2/+2 until end of turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsBlockingPredicate(),
                        "Target must be a blocking creature."
                )));
    }
}
