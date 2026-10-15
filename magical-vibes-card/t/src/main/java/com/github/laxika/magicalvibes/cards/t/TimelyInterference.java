package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.CombatRequirement;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.SetCombatRequirementThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DMU", collectorNumber = "70")
public class TimelyInterference extends Card {

    public TimelyInterference() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{1}{R}"));
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(-1, 0))
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(new Kicked(),
                        new SetCombatRequirementThisTurnEffect(CombatRequirement.MUST_BLOCK)));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
