package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.StudyCounterDrawReplacementEffect;

import java.util.List;

@CardRegistration(set = "STH", collectorNumber = "10")
public class PursuitOfKnowledge extends Card {

    public PursuitOfKnowledge() {
        addEffect(EffectSlot.STATIC, new StudyCounterDrawReplacementEffect());
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(3, CounterType.STUDY),
                        new SacrificeSelfCost(),
                        new DrawCardEffect(7)
                ),
                "Remove three study counters from Pursuit of Knowledge, Sacrifice Pursuit of Knowledge: Draw seven cards."
        ));
    }
}
