package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

/**
 * Furious Resistance — {R} Instant.
 * Target blocking creature gets +3/+0 and gains first strike until end of turn.
 */
@CardRegistration(set = "GTC", collectorNumber = "93")
public class FuriousResistance extends Card {

    public FuriousResistance() {
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsBlockingPredicate()
                )),
                "Target must be a blocking creature."
        ))
                .addSpellEffect(BoostEffect.target(3, 0))
                .addSpellEffect(new GrantKeywordEffect(Keyword.FIRST_STRIKE, Subjects.target()));
    }
}
