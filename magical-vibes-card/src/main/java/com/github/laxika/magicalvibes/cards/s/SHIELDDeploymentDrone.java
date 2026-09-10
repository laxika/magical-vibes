package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "MSH", collectorNumber = "73")
public class SHIELDDeploymentDrone extends Card {

    public SHIELDDeploymentDrone() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.whiteSoldier(1));
    }
}
