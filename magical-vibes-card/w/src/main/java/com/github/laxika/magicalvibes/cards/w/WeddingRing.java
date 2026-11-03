package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerIsActive;
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
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1883")
@CardRegistration(set = "SLD", collectorNumber = "2802")
@CardRegistration(set = "MAR", collectorNumber = "7")
@CardRegistration(set = "OMB", collectorNumber = "7")
@CardRegistration(set = "WHO", collectorNumber = "213")
@CardRegistration(set = "WHO", collectorNumber = "468")
@CardRegistration(set = "WHO", collectorNumber = "804")
@CardRegistration(set = "WHO", collectorNumber = "1059")
@CardRegistration(set = "LCC", collectorNumber = "102")
@CardRegistration(set = "VOC", collectorNumber = "32")
@CardRegistration(set = "VOC", collectorNumber = "70")
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
        )).addTrigger(Triggers.enters(ObjectRelation.THIS),
                TriggerOptions.DEFAULT.withInterveningIf(new WasCast()),
                new CreateTokenCopyOfSourceForTargetPlayerEffect(PlayerRelation.OPPONENT));
        addTrigger(Triggers.opponentDrawsCard(),
                ConditionalEffect.atTriggerTime(opponentRingDuringTheirTurn, new DrawCardEffect()));
        addTrigger(Triggers.opponentGainsLife(),
                ConditionalEffect.atTriggerTime(opponentRingDuringTheirTurn, new GainLifeEffect(new EventValue())));
    }
}
