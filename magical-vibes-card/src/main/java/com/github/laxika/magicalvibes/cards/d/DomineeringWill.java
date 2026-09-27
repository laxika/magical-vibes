package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CombatRequirement;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetCombatRequirementThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TargetGroupMarkerEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsControlOfTargetPermanentsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "C14", collectorNumber = "13")
public class DomineeringWill extends Card {

    public DomineeringWill() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addEffect(EffectSlot.SPELL, new TargetGroupMarkerEffect());

        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentIsAttackingPredicate())
                )),
                "Target must be a nonattacking creature"
        ), 0, 3)
                .addEffect(EffectSlot.SPELL,
                        new TargetPlayerGainsControlOfTargetPermanentsUntilEndOfTurnEffect(1))
                .addEffect(EffectSlot.SPELL, new UntapPermanentsEffect(TapUntapScope.ALL_TARGETS))
                .addEffect(EffectSlot.SPELL,
                        new SetCombatRequirementThisTurnEffect(
                                CombatRequirement.MUST_BLOCK, GrantScope.TARGETS));
    }
}
