package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SpellTarget;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TMC", collectorNumber = "103")
public class SplinterLeoFatherSon extends Card {

    public SplinterLeoFatherSon() {
        PlayerPredicateTargetFilter playerTarget = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY), "Target must be a player.");
        SpellTarget tokenTarget = target(playerTarget);
        SpellTarget counterTarget = target(playerTarget);

        CreateTokenForTargetPlayerEffect tokenEffect = new CreateTokenForTargetPlayerEffect(
                new CreateTokenEffect(1, "Mutant", 2, 2, CardColor.RED,
                        List.of(CardSubtype.MUTANT), Set.of(), Set.of()));
        PutCounterOnEachMatchingPermanentEffect counterEffect = new PutCounterOnEachMatchingPermanentEffect(
                CounterType.PLUS_ONE_PLUS_ONE, 1,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()))),
                EachPermanentScope.TARGET_PLAYER);

        registerEffectTargetIndex(tokenEffect, tokenTarget.getIndex());
        registerEffectTargetIndex(counterEffect, counterTarget.getIndex());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneAtTriggerTimeEffect(
                ChooseOneEffect.oneOrMore(List.of(
                        new ChooseOneEffect.ChooseOneOption(
                                "Target player creates a 2/2 red Mutant creature token.",
                                tokenEffect, playerTarget),
                        new ChooseOneEffect.ChooseOneOption(
                                "Put a +1/+1 counter on each other creature target player controls.",
                                counterEffect, playerTarget)
                ))));
    }
}
