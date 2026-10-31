package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.DuringCombat;
import com.github.laxika.magicalvibes.model.condition.FirstCombatPhase;
import com.github.laxika.magicalvibes.model.condition.NotControllerTurn;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.FirstestStrikeEffect;
import com.github.laxika.magicalvibes.model.effect.OnlySourceCreatureCanAttackThisCombatEffect;

import java.util.List;

@CardRegistration(set = "MB1", collectorNumber = "65")
public class ThroatWolf extends Card {

    public ThroatWolf() {
        // "You may cast this during an opponent's combat phase" is a permission on top of normal timing,
        // so it is modeled as a flash-style condition rather than a restriction to that window.
        setFlashCastCondition(new AllConditions(List.of(new DuringCombat(), new NotControllerTurn())));
        addStaticEffect(new FirstestStrikeEffect());
        addEffect(EffectSlot.END_OF_COMBAT_TRIGGERED,
                new ConditionalEffect(
                        new AllConditions(List.of(new FirstCombatPhase(), new NotControllerTurn())),
                        new AdditionalCombatPhaseEffect(1, new OnlySourceCreatureCanAttackThisCombatEffect())));
    }
}
