package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AttackDirection;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AttackOnlyNearestOpponentInDirectionEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.SetAttackDirectionEffect;

import java.util.List;

@CardRegistration(set = "C19", collectorNumber = "47")
public class PramikonSkyRampart extends Card {

    public PramikonSkyRampart() {
        ChooseOneEffect directionChoice = new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Left",
                        new SetAttackDirectionEffect(AttackDirection.LEFT)),
                new ChooseOneEffect.ChooseOneOption("Right",
                        new SetAttackDirectionEffect(AttackDirection.RIGHT))
        ));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneAtTriggerTimeEffect(directionChoice));
        addEffect(EffectSlot.STATIC, new AttackOnlyNearestOpponentInDirectionEffect());
    }
}
