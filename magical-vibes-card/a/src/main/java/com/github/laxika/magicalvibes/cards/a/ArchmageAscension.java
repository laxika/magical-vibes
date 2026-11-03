package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.ControllerDrewAtLeastCardsThisTurn;
import com.github.laxika.magicalvibes.model.effect.ArchmageAscensionDrawReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZEN", collectorNumber = "42")
public class ArchmageAscension extends Card {

    public ArchmageAscension() {
        addTrigger(Triggers.endStep(ActivePlayer.ANY),
                TriggerOptions.DEFAULT.withInterveningIf(new ControllerDrewAtLeastCardsThisTurn(2)),
                new MayEffect(new PutCountersOnSelfEffect(CounterType.QUEST),
                        "Put a quest counter on Archmage Ascension?"));
        addStaticEffect(new ArchmageAscensionDrawReplacementEffect());
    }
}
