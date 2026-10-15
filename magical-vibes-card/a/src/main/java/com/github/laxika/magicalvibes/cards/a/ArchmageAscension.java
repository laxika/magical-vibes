package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerDrewAtLeastCardsThisTurn;
import com.github.laxika.magicalvibes.model.effect.ArchmageAscensionDrawReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

@CardRegistration(set = "ZEN", collectorNumber = "42")
public class ArchmageAscension extends Card {

    public ArchmageAscension() {
        addEffect(EffectSlot.END_STEP_TRIGGERED, new ConditionalEffect(
                new ControllerDrewAtLeastCardsThisTurn(2),
                new MayEffect(new PutCountersOnSelfEffect(CounterType.QUEST),
                        "Put a quest counter on Archmage Ascension?")));
        addEffect(EffectSlot.STATIC, new ArchmageAscensionDrawReplacementEffect());
    }
}
