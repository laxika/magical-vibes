package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileSelfReturnAtNextTurnDeclareAttackersEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KTK", collectorNumber = "141")
@CardRegistration(set = "SLD", collectorNumber = "236")
public class MeanderingTowershell extends Card {

    public MeanderingTowershell() {
        addTrigger(Triggers.attacks(), new ExileSelfReturnAtNextTurnDeclareAttackersEffect());
    }
}
