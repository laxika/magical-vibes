package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceFirstActivatedAbilityCostForControlledCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIC", collectorNumber = "69")
@CardRegistration(set = "FIC", collectorNumber = "161")
public class ProfessorHojo extends Card {

    public ProfessorHojo() {
        addStaticEffect(new ReduceFirstActivatedAbilityCostForControlledCreatureEffect(2));
        addTrigger(Triggers.creaturesYouControlBecomeTargetOfActivatedAbility(),
                new OncePerTurnTriggerEffect(new DrawCardEffect()));
    }
}
