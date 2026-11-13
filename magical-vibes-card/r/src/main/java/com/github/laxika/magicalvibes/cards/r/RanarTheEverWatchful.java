package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceFirstForetellCostEachTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.PlayerRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "KHC", collectorNumber = "2")
public class RanarTheEverWatchful extends Card {

    public RanarTheEverWatchful() {
        addStaticEffect(new ReduceFirstForetellCostEachTurnEffect(2));
        CreateTokenEffect spirit = CreateTokenEffect.whiteSpirit(1);
        addTrigger(new TriggerCondition(Triggers.exiledFrom(Set.of(Zone.HAND),
                null, ObjectRelation.YOU_OWN)), TriggerOptions.ONE_OR_MORE, spirit);
        addTrigger(new TriggerCondition(Triggers.exiledFrom(Set.of(Zone.BATTLEFIELD),
                null, ObjectRelation.ANOTHER).causedBy(PlayerRelation.YOU)), TriggerOptions.ONE_OR_MORE, spirit);
    }
}
