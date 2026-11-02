package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.CameUnderControlThisTurn;
import com.github.laxika.magicalvibes.model.condition.DidntAttack;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "202")
public class MadDog extends Card {

    public MadDog() {
        // At the beginning of your end step, if this creature didn't attack or come under your
        // control this turn, sacrifice it.
        addTrigger(Triggers.endStep(ActivePlayer.YOU), new ConditionalEffect(
                new AllOf(List.of(new DidntAttack(), new NotCondition(new CameUnderControlThisTurn()))),
                new SacrificeSelfEffect()));
    }
}
