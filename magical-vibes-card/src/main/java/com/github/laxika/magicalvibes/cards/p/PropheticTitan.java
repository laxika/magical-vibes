package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBattlePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "209")
public class PropheticTitan extends Card {

    public PropheticTitan() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneAtTriggerTimeEffect(
                ChooseOneEffect.oneOrMoreWhen(List.of(
                        new ChooseOneEffect.ChooseOneOption(
                                "This creature deals 4 damage to any target.",
                                new DealDamageToAnyTargetEffect(4),
                                new AnyTargetPredicateTargetFilter(
                                        new PermanentAnyOfPredicate(List.of(
                                                new PermanentIsCreaturePredicate(),
                                                new PermanentIsPlaneswalkerPredicate(),
                                                new PermanentIsBattlePredicate())),
                                        new PlayerRelationPredicate(PlayerRelation.ANY),
                                        "Target must be any target")),
                        new ChooseOneEffect.ChooseOneOption(
                                "Look at the top four cards of your library. Put one of them into your hand and the rest on the bottom of your library in a random order.",
                                new LookAtTopCardsEffect(new Fixed(4), new Fixed(1), null,
                                        LookDestination.BOTTOM_OF_LIBRARY_RANDOM, false))
                ), new Delirium())));
    }
}
