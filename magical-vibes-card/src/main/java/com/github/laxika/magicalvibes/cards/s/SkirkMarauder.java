package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBattlePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "LGN", collectorNumber = "113")
public class SkirkMarauder extends Card {

    public SkirkMarauder() {
        addMorph("{2}{R}");
        target(new AnyTargetPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate(),
                        new PermanentIsBattlePredicate())),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be any target"
        )).addEffect(EffectSlot.ON_TURNED_FACE_UP, new DealDamageToAnyTargetEffect(2));
    }
}
