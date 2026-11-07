package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.AttackingPlayerIsOpponent;
import com.github.laxika.magicalvibes.model.condition.SourceIsTapped;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedByAtMostNCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.MakeChosenPermanentAttackingEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsControlOfSourceCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


@CardRegistration(set = "C19", collectorNumber = "50")
public class TahngarthFirstMate extends Card {

    public TahngarthFirstMate() {
        addStaticEffect(new CanBeBlockedByAtMostNCreaturesEffect(1));
        addTrigger(Triggers.playerAttacks(),
                TriggerOptions.DEFAULT.withEventCondition(new AttackingPlayerIsOpponent())
                        .withInterveningIf(new SourceIsTapped()),
                new MayEffect(
                        TargetPlayerGainsControlOfSourceCreatureEffect.triggeringPlayer(
                                ControlDuration.END_OF_COMBAT,
                                MakeChosenPermanentAttackingEffect.attackingTargetOfTriggeringPlayer()),
                        "Have that opponent gain control of Tahngarth until end of combat?"));
    }
}
