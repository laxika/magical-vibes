package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesPermanentUnlessDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.PossessedPortalDrawReplacementEffect;

@CardRegistration(set = "5DN", collectorNumber = "144")
public class PossessedPortal extends Card {

    public PossessedPortal() {
        addEffect(EffectSlot.STATIC, new PossessedPortalDrawReplacementEffect());
        addEffect(EffectSlot.END_STEP_TRIGGERED, new EachPlayerSacrificesPermanentUnlessDiscardEffect());
    }
}
