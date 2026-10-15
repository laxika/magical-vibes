package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.AttackingPlayerIsOpponent;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackers;
import com.github.laxika.magicalvibes.model.condition.SourceIsTapped;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedByAtMostNCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.MakeChosenPermanentAttackingEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsControlOfSourceCreatureEffect;

import java.util.List;

@CardRegistration(set = "C19", collectorNumber = "50")
public class TahngarthFirstMate extends Card {

    public TahngarthFirstMate() {
        addEffect(EffectSlot.STATIC, new CanBeBlockedByAtMostNCreaturesEffect(1));
        addEffect(EffectSlot.ON_ANY_PLAYER_ATTACKS, new ConditionalEffect(
                new AllOf(List.of(
                        new AttackingPlayerIsOpponent(),
                        new MinimumAttackers(1),
                        new SourceIsTapped()
                )),
                new MayEffect(
                        TargetPlayerGainsControlOfSourceCreatureEffect.triggeringPlayer(
                                ControlDuration.END_OF_COMBAT,
                                MakeChosenPermanentAttackingEffect.attackingTargetOfTriggeringPlayer()),
                        "Have that opponent gain control of Tahngarth until end of combat?")));
    }
}
