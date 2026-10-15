package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceFirstActivatedAbilityCostForControlledCreatureEffect;

@CardRegistration(set = "FIC", collectorNumber = "69")
@CardRegistration(set = "FIC", collectorNumber = "161")
public class ProfessorHojo extends Card {

    public ProfessorHojo() {
        addEffect(EffectSlot.STATIC, new ReduceFirstActivatedAbilityCostForControlledCreatureEffect(2));
        addEffect(EffectSlot.ON_ALLY_CREATURE_BECOMES_TARGET_OF_ACTIVATED_ABILITY,
                new OncePerTurnTriggerEffect(new DrawCardEffect()));
    }
}
