package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayCastFromTargetPlayerHandWithoutPayingManaCostEffect;

@CardRegistration(set = "RAV", collectorNumber = "215")
public class MindleechMass extends Card {

    public MindleechMass() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                MayCastFromTargetPlayerHandWithoutPayingManaCostEffect.damagedPlayer());
    }
}
