package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.SpellCastTimingRestriction;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesMustAttackThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedEndStepTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAttackedThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByActivePlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledContinuouslySinceBeginningOfTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "4ED", collectorNumber = "101")
@CardRegistration(set = "SUM", collectorNumber = "80")
@CardRegistration(set = "3ED", collectorNumber = "80")
@CardRegistration(set = "2ED", collectorNumber = "78")
@CardRegistration(set = "LEA", collectorNumber = "77")
@CardRegistration(set = "LEB", collectorNumber = "78")
public class SirensCall extends Card {

    public SirensCall() {
        setSpellCastTimingRestriction(SpellCastTimingRestriction.OPPONENTS_TURN_BEFORE_ATTACKERS);

        // The requirement modifies the rules, so it also binds creatures the active player gains later this turn
        // (CR 611.2c).
        addSpellEffect(new MatchingCreaturesMustAttackThisTurnEffect(new PermanentControlledByActivePlayerPredicate()));
        addSpellEffect(RegisterDelayedEndStepTriggerEffect.aboutActivePlayer(new DestroyAllPermanentsEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.WALL)),
                        new PermanentNotPredicate(new PermanentAttackedThisTurnPredicate()),
                        new PermanentControlledContinuouslySinceBeginningOfTurnPredicate())),
                EachPermanentScope.TARGET_PLAYER)));
    }
}
