package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBattlePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "163b")
public class MishraLostToPhyrexia extends Card {

    public MishraLostToPhyrexia() {
        ChooseOneEffect modes = chooseThreeModes();
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, modes);
        addEffect(EffectSlot.ON_ATTACK, modes);
    }

    private ChooseOneEffect chooseThreeModes() {
        DiscardEffect discard = new DiscardEffect(2, DiscardRecipient.TARGET_PLAYER);
        DealDamageToAnyTargetEffect damage = new DealDamageToAnyTargetEffect(3);
        DestroyTargetPermanentEffect destroy = new DestroyTargetPermanentEffect(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsPlaneswalkerPredicate())));

        registerEffectTargetIndex(discard, target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT), "Target must be an opponent")).getIndex());
        registerEffectTargetIndex(damage, target(new AnyTargetPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate(),
                        new PermanentIsBattlePredicate())),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a creature, planeswalker, battle, or player")).getIndex());
        registerEffectTargetIndex(destroy, target(new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsPlaneswalkerPredicate())),
                "Target must be an artifact or planeswalker")).getIndex());

        return new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target opponent discards two cards", discard),
                new ChooseOneEffect.ChooseOneOption(
                        "Mishra deals 3 damage to any target", damage),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target artifact or planeswalker", destroy),
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures you control gain menace and trample until end of turn",
                        List.of(
                                new GrantKeywordEffect(Keyword.MENACE, GrantScope.OWN_CREATURES),
                                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.OWN_CREATURES))),
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures you don't control get -1/-1 until end of turn",
                        new BoostAllCreaturesEffect(-1, -1,
                                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))),
                new ChooseOneEffect.ChooseOneOption(
                        "Create two tapped Powerstone tokens", CreateTokenEffect.ofPowerstoneToken(new Fixed(2))
                )), 3);
    }
}
