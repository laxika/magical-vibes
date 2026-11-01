package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "HA6", collectorNumber = "5")
@CardRegistration(set = "SOC", collectorNumber = "246")
@CardRegistration(set = "C21", collectorNumber = "53")
public class LaeliaTheBladeReforged extends Card {

    public LaeliaTheBladeReforged() {
        addEffect(EffectSlot.ON_ATTACK, new ExileTopCardMayPlayThisTurnEffect(false));
        addTrigger(new TriggerCondition(Triggers.exiledFrom(Set.of(Zone.LIBRARY, Zone.GRAVEYARD),
                null, ObjectRelation.YOU_OWN)), TriggerOptions.ONE_OR_MORE,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
