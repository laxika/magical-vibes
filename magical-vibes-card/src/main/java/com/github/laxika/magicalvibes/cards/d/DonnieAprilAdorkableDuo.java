package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerReturnsCardFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "TMC", collectorNumber = "111")
public class DonnieAprilAdorkableDuo extends Card {

    public DonnieAprilAdorkableDuo() {
        PlayerPredicateTargetFilter playerTarget = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY), "Target must be a player");
        CardAnyOfPredicate artifactInstantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.ARTIFACT),
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));

        ChooseOneEffect modes = new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target player draws two cards.",
                        new DrawCardForTargetPlayerEffect(2, false, true), playerTarget),
                new ChooseOneEffect.ChooseOneOption(
                        "Target player returns an artifact, instant, or sorcery card from their graveyard to their hand.",
                        new TargetPlayerReturnsCardFromGraveyardToHandEffect(artifactInstantOrSorcery), playerTarget)
        ), false, 1, 2, false);
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneAtTriggerTimeEffect(modes));
    }
}
