package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerControlsCreaturesThenPerpetuallyBoostEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsHandThenSeeksEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "49")
public class ChaosBalor extends Card {

    public ChaosBalor() {
        setMultiTargetConstraint(MultiTargetConstraint.DISTINCT_TARGETS);
        var playerTarget = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY), "Target must be a player.");
        var nonlandCard = new CardNotPredicate(new CardTypePredicate(CardType.LAND));
        ChooseOneEffect modes = new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target player discards all the cards in their hand, then seeks that many nonland cards.",
                        new TargetPlayerDiscardsHandThenSeeksEffect(nonlandCard), playerTarget),
                new ChooseOneEffect.ChooseOneOption(
                        "Chaos Balor deals 2 damage to target player and they create two Treasure tokens.",
                        List.of(
                                new DealDamageToTargetPlayerOrPlaneswalkerEffect(2),
                                new CreateTokenForTargetPlayerEffect(CreateTokenEffect.ofTreasureToken(2))),
                        playerTarget),
                new ChooseOneEffect.ChooseOneOption(
                        "Chaos Balor deals 2 damage to each creature target player controls. Those creatures perpetually get +2/+0.",
                        new DealDamageToTargetPlayerControlsCreaturesThenPerpetuallyBoostEffect(2, 2, 0),
                        playerTarget)
        ), 2);

        addEffect(EffectSlot.ON_ATTACK, new ChooseOneAtTriggerTimeEffect(modes));
        addEffect(EffectSlot.ON_DEATH, new ChooseOneAtTriggerTimeEffect(modes));
    }
}
