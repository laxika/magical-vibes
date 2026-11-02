package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "10E", collectorNumber = "246")
@CardRegistration(set = "ULG", collectorNumber = "96")
public class ViashinoSandscout extends Card {

    public ViashinoSandscout() {
        addTrigger(Triggers.endStep(ActivePlayer.ANY), ReturnToHandEffect.self());
    }
}
