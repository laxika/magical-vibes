package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "51")
public class CleverConjurer extends Card {

    public CleverConjurer() {
        PermanentPredicate notCleverConjurer = new PermanentNotPredicate(
                new PermanentNamedPredicate("Clever Conjurer"));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new UntapPermanentsEffect(TapUntapScope.TARGET, notCleverConjurer)),
                "Mage Hand — {T}: Untap target permanent not named Clever Conjurer. Activate only as a sorcery.",
                new PermanentPredicateTargetFilter(
                        notCleverConjurer,
                        "Target must be a permanent not named Clever Conjurer"),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
