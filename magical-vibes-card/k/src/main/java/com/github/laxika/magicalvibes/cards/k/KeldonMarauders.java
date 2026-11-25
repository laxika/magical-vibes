package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PLC", collectorNumber = "102")
@CardRegistration(set = "PD2", collectorNumber = "7")
@CardRegistration(set = "EMA", collectorNumber = "136")
public class KeldonMarauders extends Card {

    public KeldonMarauders() {
        target(new AnyTargetPredicateTargetFilter(
                new PermanentIsPlaneswalkerPredicate(),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player or planeswalker"))
                .addStaticEffect(new EnterWithCountersEffect(CounterType.TIME, new Fixed(2)))
                .addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                        TriggerOptions.DEFAULT.withInterveningIf(new SourceCounterThreshold(1, CounterType.TIME)),
                        RemoveCountersEffect.fromSelf(CounterType.TIME, 1))
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        DealDamageEffect.toTargetPlayerOrPlaneswalker(1))
                .addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS),
                        DealDamageEffect.toTargetPlayerOrPlaneswalker(1));
    }
}
