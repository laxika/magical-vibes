package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesPermanentUnlessDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.PossessedPortalDrawReplacementEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "5DN", collectorNumber = "144")
public class PossessedPortal extends Card {

    public PossessedPortal() {
        addStaticEffect(new PossessedPortalDrawReplacementEffect());
        addTrigger(Triggers.endStep(ActivePlayer.ANY), new EachPlayerSacrificesPermanentUnlessDiscardEffect());
    }
}
