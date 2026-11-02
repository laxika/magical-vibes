package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MH1", collectorNumber = "153")
public class ViashinoSandsprinter extends Card {

    public ViashinoSandsprinter() {
        addTrigger(Triggers.endStep(ActivePlayer.ANY), ReturnToHandEffect.self());
        addCycling("{R}");
    }
}
