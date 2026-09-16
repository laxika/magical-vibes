package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerIsActivePlayer;
import com.github.laxika.magicalvibes.model.condition.WasCast;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "OMB", collectorNumber = "7")
public class WeddingRing extends Card {

    private static final PermanentAllOfPredicate WEDDING_RING = new PermanentAllOfPredicate(List.of(
            new PermanentIsArtifactPredicate(),
            new PermanentNamedPredicate("Wedding Ring")));

    public WeddingRing() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent")).addEffect(
                EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(new WasCast(), new CreateTokenCopyOfSourceForTargetPlayerEffect()));

        addEffect(EffectSlot.ON_OPPONENT_DRAWS, new ConditionalEffect(
                new AllConditions(List.of(
                        new OpponentControlsPermanent(WEDDING_RING),
                        new TargetPlayerIsActivePlayer())),
                new DrawCardEffect(1)));

        addEffect(EffectSlot.ON_OPPONENT_GAINS_LIFE, new ConditionalEffect(
                new AllConditions(List.of(
                        new OpponentControlsPermanent(WEDDING_RING),
                        new TargetPlayerIsActivePlayer())),
                new GainLifeEffect(new EventValue())));
    }
}
