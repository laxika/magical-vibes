package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerTurn;
import com.github.laxika.magicalvibes.model.condition.WasCast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "MAR", collectorNumber = "7")
public class WeddingRing extends Card {

    public WeddingRing() {
        PermanentAllOfPredicate weddingRing = new PermanentAllOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentNamedPredicate("Wedding Ring")));
        AllConditions opponentRingDuringTheirTurn = new AllConditions(List.of(
                new TargetPlayerControlsPermanent(weddingRing),
                new TargetPlayerTurn()));

        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(new WasCast(),
                        new CreateTokenCopyOfSourceForTargetPlayerEffect(PlayerRelation.OPPONENT)));
        addEffect(EffectSlot.ON_OPPONENT_DRAWS,
                new ConditionalEffect(opponentRingDuringTheirTurn, new DrawCardEffect()));
        addEffect(EffectSlot.ON_OPPONENT_GAINS_LIFE,
                new ConditionalEffect(opponentRingDuringTheirTurn, new GainLifeEffect(new EventValue())));
    }
}
