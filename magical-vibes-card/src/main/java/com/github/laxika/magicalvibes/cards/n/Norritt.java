package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetIfDidNotAttackAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByActivePlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledContinuouslySinceBeginningOfTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ICE", collectorNumber = "155")
public class Norritt extends Card {

    public Norritt() {
        // {T}: Untap target blue creature.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new UntapPermanentsEffect(TapUntapScope.TARGET)),
                "{T}: Untap target blue creature.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentColorInPredicate(Set.of(CardColor.BLUE)))),
                        "Target must be a blue creature")));

        // {T}: Choose target non-Wall creature the active player has controlled continuously since
        // the beginning of the turn. That creature attacks this turn if able. Destroy it at the
        // beginning of the next end step if it didn't attack this turn. Activate only before
        // attackers are declared.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new MustAttackThisTurnEffect(false),
                        new DestroyTargetIfDidNotAttackAtEndStepEffect()),
                "{T}: Choose target non-Wall creature the active player has controlled continuously since the beginning of the turn. That creature attacks this turn if able. Destroy it at the beginning of the next end step if it didn't attack this turn. Activate only before attackers are declared.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.WALL)),
                                new PermanentControlledByActivePlayerPredicate(),
                                new PermanentControlledContinuouslySinceBeginningOfTurnPredicate())),
                        "Target must be a non-Wall creature the active player has controlled continuously since the beginning of the turn"),
                null,
                null,
                ActivationTimingRestriction.BEFORE_ATTACKERS_DECLARED));
    }
}
