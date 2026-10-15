package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EndureEffect;

@CardRegistration(set = "TDM", collectorNumber = "12")
public class FortressKinGuard extends Card {

    public FortressKinGuard() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EndureEffect(1));
    }
}
