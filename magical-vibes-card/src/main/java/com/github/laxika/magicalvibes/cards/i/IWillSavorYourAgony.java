package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "DSC", collectorNumber = "338")
public class IWillSavorYourAgony extends Card {

    public IWillSavorYourAgony() {
        var creatureTarget = new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(), "Target must be a creature.");
        var playerTarget = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY), "Target must be a player.");

        addEffect(EffectSlot.SPELL, new ChooseOneAtTriggerTimeEffect(
                ChooseOneEffect.withRepeatedModes(List.of(
                        new ChooseOneEffect.ChooseOneOption(
                                "Destroy target creature.", new DestroyTargetPermanentEffect(), creatureTarget),
                        new ChooseOneEffect.ChooseOneOption(
                                "Target player draws a card.",
                                new DrawCardForTargetPlayerEffect(1, false, true), playerTarget),
                        new ChooseOneEffect.ChooseOneOption(
                                "Target player gains 5 life.", new TargetPlayerGainsLifeEffect(5), playerTarget)
                ), 3)));
    }
}
