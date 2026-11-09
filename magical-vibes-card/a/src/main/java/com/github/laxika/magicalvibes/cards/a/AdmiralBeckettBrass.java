package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControllerDealtCombatDamageBySubtypeThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "XLN", collectorNumber = "217")
@CardRegistration(set = "SLD", collectorNumber = "1410")
@CardRegistration(set = "SLD", collectorNumber = "1561")
@CardRegistration(set = "LCC", collectorNumber = "264")
public class AdmiralBeckettBrass extends Card {

    public AdmiralBeckettBrass() {
        // Other Pirates you control get +1/+1.
        addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.PIRATE))));

        // At the beginning of your end step, gain control of target nonland permanent
        // controlled by a player who was dealt combat damage by three or more Pirates this turn.
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentNotPredicate(new PermanentIsLandPredicate()),
                        new PermanentControllerDealtCombatDamageBySubtypeThisTurnPredicate(CardSubtype.PIRATE, 3))),
                "Target must be a nonland permanent controlled by a player dealt combat damage by three or more "
                        + "Pirates this turn"))
                .addTrigger(Triggers.endStep(ActivePlayer.YOU),
                        new GainControlOfTargetEffect(ControlDuration.PERMANENT));
    }
}
