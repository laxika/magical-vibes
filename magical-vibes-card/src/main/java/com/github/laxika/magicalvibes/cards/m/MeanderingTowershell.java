package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileSelfReturnAtNextTurnDeclareAttackersEffect;

@CardRegistration(set = "KTK", collectorNumber = "141")
public class MeanderingTowershell extends Card {

    public MeanderingTowershell() {
        addEffect(EffectSlot.ON_ATTACK, new ExileSelfReturnAtNextTurnDeclareAttackersEffect());
    }
}
