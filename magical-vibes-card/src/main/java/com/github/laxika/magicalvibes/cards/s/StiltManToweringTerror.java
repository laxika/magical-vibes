package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeSacrificedEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToTargetUntilEndOfYourNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByDefendingPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "61")
@CardRegistration(set = "MSC", collectorNumber = "373")
public class StiltManToweringTerror extends Card {

    public StiltManToweringTerror() {
        PermanentPredicate targetPredicate = new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentIsCreaturePredicate()),
                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                new PermanentControlledByDefendingPlayerPredicate()));

        target(new PermanentPredicateTargetFilter(
                targetPredicate, "Target must be a noncreature, nonland permanent that player controls"))
                .addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                        new AllyCombatDamageTriggerEffect(
                                new PermanentHasSubtypePredicate(CardSubtype.VILLAIN),
                                SequenceEffect.of(
                                        GainControlOfTargetEffect.withTargetPredicate(
                                                ControlDuration.UNTIL_END_OF_YOUR_NEXT_TURN, targetPredicate),
                                        new GrantStaticEffectToTargetUntilEndOfYourNextTurnEffect(
                                                new CantBeSacrificedEffect())),
                                false,
                                true));
    }
}
