package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBattlePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "STX", collectorNumber = "199")
public class LoreholdCommand extends Card {

    public LoreholdCommand() {
        AnyTargetPredicateTargetFilter anyTarget = new AnyTargetPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate(),
                        new PermanentIsBattlePredicate())),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a creature, planeswalker, battle, or player.");

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Create a 3/2 red and white Spirit creature token",
                        new CreateTokenEffect("Spirit", 3, 2, CardColor.RED,
                                Set.of(CardColor.RED, CardColor.WHITE), List.of(CardSubtype.SPIRIT))),
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures you control get +1/+0 and gain indestructible and haste until end of turn",
                        List.of(
                                new BoostAllOwnCreaturesEffect(1, 0),
                                new GrantKeywordEffect(Set.of(Keyword.INDESTRUCTIBLE, Keyword.HASTE),
                                        GrantScope.OWN_CREATURES))),
                new ChooseOneEffect.ChooseOneOption(
                        "Lorehold Command deals 3 damage to any target. Target player gains 3 life",
                        List.of(
                                new DealDamageToAnyTargetEffect(3),
                                new TargetPlayerGainsLifeEffect(3)),
                        List.of(anyTarget, new PlayerPredicateTargetFilter(
                                new PlayerRelationPredicate(PlayerRelation.ANY),
                                "Target must be a player."))),
                new ChooseOneEffect.ChooseOneOption(
                        "Sacrifice a permanent, then draw two cards",
                        new SacrificePermanentThenEffect(
                                new PermanentTruePredicate(), new DrawCardEffect(2), "a permanent"))
        ), 2));
    }
}
