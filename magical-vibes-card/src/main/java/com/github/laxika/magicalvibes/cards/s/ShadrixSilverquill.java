package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "STX", collectorNumber = "230")
public class ShadrixSilverquill extends Card {

    public ShadrixSilverquill() {
        var anyPlayer = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player.");

        var tokenEffect = new CreateTokenForTargetPlayerEffect(new CreateTokenEffect(
                1, "Inkling", 2, 1, CardColor.WHITE,
                Set.of(CardColor.WHITE, CardColor.BLACK), List.of(CardSubtype.INKLING),
                Set.of(Keyword.FLYING), Set.of()));
        var drawEffect = new DrawCardForTargetPlayerEffect(1, false, true);
        var loseLifeEffect = new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER);
        var counterEffect = new PutCounterOnEachMatchingPermanentEffect(
                CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsCreaturePredicate(),
                EachPermanentScope.TARGET_PLAYER);

        registerEffectTargetIndex(tokenEffect, target(anyPlayer).getIndex());
        registerEffectTargetIndex(drawEffect, target(anyPlayer).getIndex());
        registerEffectTargetIndex(loseLifeEffect, getSpellTargets().get(1).getIndex());
        registerEffectTargetIndex(counterEffect, target(anyPlayer).getIndex());

        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target player creates a 2/1 white and black Inkling creature token with flying",
                        tokenEffect, anyPlayer),
                new ChooseOneEffect.ChooseOneOption(
                        "Target player draws a card and loses 1 life",
                        List.of(drawEffect, loseLifeEffect), anyPlayer),
                new ChooseOneEffect.ChooseOneOption(
                        "Target player puts a +1/+1 counter on each creature they control",
                        counterEffect, anyPlayer)
        ), true, 2, 2));
    }
}
