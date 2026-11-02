package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.SourceIsTapped;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SurvivalTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSK", collectorNumber = "1")
public class AcrobaticCheerleader extends Card {

    public AcrobaticCheerleader() {
        addTrigger(Triggers.postcombatMainPhase(ActivePlayer.YOU),
                new ConditionalEffect(new SourceIsTapped(),
                        new com.github.laxika.magicalvibes.model.effect.OnceOnlyTriggerEffect(
                                new PutCountersOnSelfEffect(CounterType.FLYING))));
    }
}
