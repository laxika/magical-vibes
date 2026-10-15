package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.MustAttackUnlessControllerPaysManaValueEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByActivePlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledContinuouslySinceBeginningOfTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "311")
public class ArcumsWhistle extends Card {

    public ArcumsWhistle() {
        // {3}, {T}: Choose target non-Wall creature the active player has controlled continuously
        // since the beginning of the turn. That player may pay {X}, where X is that creature's mana
        // value. If they don't pay, the creature attacks this turn if able, and at the beginning of
        // the next end step, destroy it if it didn't attack this turn. Activate only before
        // attackers are declared.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new MustAttackUnlessControllerPaysManaValueEffect()),
                "{3}, {T}: Choose target non-Wall creature the active player has controlled continuously since the beginning of the turn. That player may pay {X}, where X is that creature's mana value. If they don't pay, the creature attacks this turn if able, and at the beginning of the next end step, destroy it if it didn't attack this turn. Activate only before attackers are declared.",
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
