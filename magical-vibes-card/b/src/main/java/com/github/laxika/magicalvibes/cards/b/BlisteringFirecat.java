package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONS", collectorNumber = "189")
public class BlisteringFirecat extends Card {

    public BlisteringFirecat() {
        addMorph("{R}{R}");
        addTrigger(Triggers.endStep(ActivePlayer.ANY), new SacrificeSelfEffect());
    }
}
