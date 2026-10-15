package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBattlePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "55")
@CardRegistration(set = "PIP", collectorNumber = "583")
public class BottleCapBlast extends Card {

    public BottleCapBlast() {
        target(new AnyTargetPredicateTargetFilter(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsPlaneswalkerPredicate(),
                                new PermanentIsBattlePredicate())),
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be any target"))
                .addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(5))
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(
                        new EventValueAtLeast(1), CreateTokenEffect.ofTappedTreasureToken(new EventValue())));
    }
}
