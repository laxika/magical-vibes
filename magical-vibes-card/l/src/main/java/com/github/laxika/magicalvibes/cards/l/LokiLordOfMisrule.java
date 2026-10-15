package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachOtherPermanentMatchingPredicateBecomesCopyOfTargetPermanentUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "628")
public class LokiLordOfMisrule extends Card {

    private static final PermanentPredicate CREATURES_YOU_CONTROL = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentControlledBySourceControllerPredicate()));

    public LokiLordOfMisrule() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}",
                List.of(new EachOtherPermanentMatchingPredicateBecomesCopyOfTargetPermanentUntilEndOfTurnEffect(
                        CREATURES_YOU_CONTROL, CREATURES_YOU_CONTROL, true)),
                "{U}, {T}: Choose target creature you control. Each creature you control other than the chosen creature becomes a copy of that creature until end of turn, except it isn't legendary. Activate only as a sorcery.",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(), "Target must be a creature you control"),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
