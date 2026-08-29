package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeSaddledUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DFT", collectorNumber = "233")
public class GuidelightMatrix extends Card {

    public GuidelightMatrix() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect());

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new BecomeSaddledUntilEndOfTurnEffect(GrantScope.TARGET)),
                "{2}, {T}: Target Mount you control becomes saddled until end of turn. Activate only as a sorcery.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentControlledBySourceControllerPredicate(),
                                new PermanentHasSubtypePredicate(CardSubtype.MOUNT))),
                        "Target must be a Mount you control"),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new AnimatePermanentsEffect(
                        null, null, List.of(), Set.of(), null, Set.of(),
                        GrantScope.TARGET, EffectDuration.UNTIL_END_OF_TURN, null)),
                "{2}, {T}: Target Vehicle you control becomes an artifact creature until end of turn. Activate only as a sorcery.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentControlledBySourceControllerPredicate(),
                                new PermanentHasSubtypePredicate(CardSubtype.VEHICLE))),
                        "Target must be a Vehicle you control"),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
