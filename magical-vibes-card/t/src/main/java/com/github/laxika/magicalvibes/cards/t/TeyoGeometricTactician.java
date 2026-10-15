package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.RestrictAttacksToDirectionUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.RestrictAttacksToDirectionUntilNextTurnEffect.Direction;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "CMM", collectorNumber = "725")
public class TeyoGeometricTactician extends Card {

    public TeyoGeometricTactician() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                "Wall", 0, 4, CardColor.WHITE, List.of(CardSubtype.WALL),
                Set.of(Keyword.DEFENDER, Keyword.FLYING), Set.of()));

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new DrawCardEffect(), new DrawCardForTargetPlayerEffect(1, false, true)),
                "+1: You and target opponent each draw a card.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent")));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption("Left",
                                new RestrictAttacksToDirectionUntilNextTurnEffect(Direction.LEFT)),
                        new ChooseOneEffect.ChooseOneOption("Right",
                                new RestrictAttacksToDirectionUntilNextTurnEffect(Direction.RIGHT))))),
                "−2: Choose left or right. Until your next turn, each player may attack only the nearest opponent in the last chosen direction and planeswalkers controlled by that opponent."));
    }
}
