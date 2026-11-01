package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YTDM", collectorNumber = "8")
public class RunebladeRaiser extends Card {

    public RunebladeRaiser() {
        addStaticEffect(new EntersTappedEffect());
        addTrigger(Triggers.dies(ObjectRelation.THIS),
                new ReturnSourceCardFromGraveyardToBattlefieldEffect(false, false, true));
    }
}
