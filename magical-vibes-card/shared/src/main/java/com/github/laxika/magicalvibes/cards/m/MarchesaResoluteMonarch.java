package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerWasNotDealtCombatDamageSinceLastTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

public class MarchesaResoluteMonarch extends Card {

    public MarchesaResoluteMonarch() {
        target(TargetFilters.permanent(), 0, 1)
                .addEffect(EffectSlot.ON_ATTACK, new RemoveAllCountersFromTargetPermanentEffect());
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new ControllerWasNotDealtCombatDamageSinceLastTurn(),
                SequenceEffect.of(new DrawCardEffect(1), new LoseLifeEffect(1))));
    }
}
