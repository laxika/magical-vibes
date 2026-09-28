package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCommanderFromCommandZoneOntoBattlefieldEffect;

@CardRegistration(set = "TDC", collectorNumber = "218")
public class HellkiteCourser extends Card {

    public HellkiteCourser() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new PutCommanderFromCommandZoneOntoBattlefieldEffect(),
                "Put a commander you own from the command zone onto the battlefield?"));
    }
}
