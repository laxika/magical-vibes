package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerIsActive;
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

@CardRegistration(set = "SLD", collectorNumber = "1883")
@CardRegistration(set = "SLD", collectorNumber = "2802")
public class WeddingRing extends Card {

    private static final PermanentAllOfPredicate WEDDING_RING_FILTER = new PermanentAllOfPredicate(List.of(
            new PermanentIsArtifactPredicate(),
            new PermanentNamedPredicate("Wedding Ring")));
    private static final AllConditions OPPONENT_WEDDING_RING_DURING_TURN = new AllConditions(List.of(
            new TargetPlayerControlsPermanent(WEDDING_RING_FILTER),
            new TargetPlayerIsActive()));

    public WeddingRing() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent")).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(new WasCast(), new CreateTokenCopyOfSourceForTargetPlayerEffect()));
        addEffect(EffectSlot.ON_OPPONENT_DRAWS, new ConditionalEffect(
                OPPONENT_WEDDING_RING_DURING_TURN, new DrawCardEffect()));
        addEffect(EffectSlot.ON_OPPONENT_GAINS_LIFE, new ConditionalEffect(
                OPPONENT_WEDDING_RING_DURING_TURN, new GainLifeEffect(new EventValue())));
    }
}
