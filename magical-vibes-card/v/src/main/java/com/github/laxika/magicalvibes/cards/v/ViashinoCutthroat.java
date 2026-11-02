package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ULG", collectorNumber = "94")
public class ViashinoCutthroat extends Card {

    public ViashinoCutthroat() {
        addTrigger(Triggers.endStep(ActivePlayer.ANY), ReturnToHandEffect.self());
    }
}
