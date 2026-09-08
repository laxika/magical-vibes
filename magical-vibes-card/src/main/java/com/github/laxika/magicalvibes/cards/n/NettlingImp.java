package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.NotControllerTurn;
import com.github.laxika.magicalvibes.model.effect.CombatRequirement;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetIfDidNotAttackAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.SetCombatRequirementThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByActivePlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledContinuouslySinceBeginningOfTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "SUM", collectorNumber = "119")
public class NettlingImp extends Card {

    public NettlingImp() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SetCombatRequirementThisTurnEffect(CombatRequirement.MUST_ATTACK),
                        new DestroyTargetIfDidNotAttackAtEndStepEffect()),
                "{T}: Choose target non-Wall creature the active player has controlled continuously since the beginning of the turn. That creature attacks this turn if able. Destroy it at the beginning of the next end step if it didn't attack this turn. Activate only during an opponent's turn, before attackers are declared.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.WALL)),
                                new PermanentControlledByActivePlayerPredicate(),
                                new PermanentControlledContinuouslySinceBeginningOfTurnPredicate())),
                        "Target must be a non-Wall creature the active player has controlled continuously since the beginning of the turn"),
                null,
                null,
                ActivationTimingRestriction.BEFORE_ATTACKERS_DECLARED
        ).withActivationCondition(new NotControllerTurn(),
                "This ability can only be activated during an opponent's turn"));
    }
}
