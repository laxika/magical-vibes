package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterAndSacrificeSelfOnLastEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "16")
@CardRegistration(set = "WHO", collectorNumber = "336")
@CardRegistration(set = "WHO", collectorNumber = "621")
@CardRegistration(set = "WHO", collectorNumber = "927")
public class CrackInTime extends Card {

    public CrackInTime() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.TIME, new Fixed(3)));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new RemoveCounterAndSacrificeSelfOnLastEffect(CounterType.TIME));
        ExileTargetPermanentUntilSourceLeavesEffect exileEffect =
                new ExileTargetPermanentUntilSourceLeavesEffect();
        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), exileEffect)
                .addTrigger(Triggers.precombatMainPhase(ActivePlayer.YOU), exileEffect);
    }
}
