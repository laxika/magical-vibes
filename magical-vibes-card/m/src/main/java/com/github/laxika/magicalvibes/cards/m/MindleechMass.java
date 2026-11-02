package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayCastFromTargetPlayerHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RAV", collectorNumber = "215")
@CardRegistration(set = "RVR", collectorNumber = "201")
public class MindleechMass extends Card {

    public MindleechMass() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                MayCastFromTargetPlayerHandWithoutPayingManaCostEffect.damagedPlayerMayLook());
    }
}
