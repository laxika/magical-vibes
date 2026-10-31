package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.amount.TargetToughness;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "8ED", collectorNumber = "162")
@CardRegistration(set = "MMQ", collectorNumber = "159")
@CardRegistration(set = "PHUK", collectorNumber = "15")
@CardRegistration(set = "PHUK", collectorNumber = "16")
public class SeverSoul extends Card {

    public SeverSoul() {
        // Destroy target nonblack creature. It can't be regenerated. You gain life equal to its
        // toughness.
        //
        // The effects resolve in printed order; the life gain reads the destroyed creature's
        // last-known toughness.
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentColorInPredicate(Set.of(CardColor.BLACK)))
                )),
                "Target must be a nonblack creature"
        ))
                .addSpellEffect(new DestroyTargetPermanentEffect(true))
                .addSpellEffect(new GainLifeEffect(new TargetToughness()));
    }
}
