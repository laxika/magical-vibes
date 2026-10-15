package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TurnTargetFaceUpEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsFaceDownPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTurnedFaceUpThisTurnPredicate;

import java.util.List;

@CardRegistration(set = "MKC", collectorNumber = "1")
@CardRegistration(set = "MKC", collectorNumber = "49")
@CardRegistration(set = "MKC", collectorNumber = "314")
public class KaustEyesOfTheGlade extends Card {

    public KaustEyesOfTheGlade() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        new PermanentTurnedFaceUpThisTurnPredicate(), new DrawCardEffect(1)));

        PermanentAllOfPredicate targetPredicate = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsFaceDownPredicate(),
                new PermanentIsAttackingPredicate()));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new TurnTargetFaceUpEffect()),
                "{T}: Turn target face-down attacking creature you control face up.",
                new ControlledPermanentPredicateTargetFilter(
                        targetPredicate, "Target must be a face-down attacking creature you control")));
    }
}
