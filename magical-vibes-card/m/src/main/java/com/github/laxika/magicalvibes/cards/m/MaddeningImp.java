package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesMustAttackThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedEndStepTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAttackedThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByActivePlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "143")
public class MaddeningImp extends Card {

    public MaddeningImp() {
        PermanentNotPredicate nonWall = new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.WALL));
        // "Those creatures" are the ones the requirement binds: the active player's non-Wall creatures, including
        // ones they gain later this turn (CR 611.2c).
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new MatchingCreaturesMustAttackThisTurnEffect(new PermanentAllOfPredicate(List.of(
                                new PermanentControlledByActivePlayerPredicate(), nonWall))),
                        RegisterDelayedEndStepTriggerEffect.aboutActivePlayer(DestroyTargetPermanentEffect.allTargetPlayerControls(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        nonWall,
                                        new PermanentNotPredicate(new PermanentAttackedThisTurnPredicate())))))),
                "{T}: Non-Wall creatures the active player controls attack this turn if able. At the beginning of the next end step, destroy each of those creatures that didn't attack this turn. Activate only during an opponent's turn and only before combat.",
                ActivationTimingRestriction.ONLY_DURING_OPPONENTS_TURN_BEFORE_COMBAT));
    }
}
