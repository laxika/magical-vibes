package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M21", collectorNumber = "72")
@CardRegistration(set = "HBG", collectorNumber = "914")
public class SpinedMegalodon extends Card {

    public SpinedMegalodon() {
        addTrigger(Triggers.attacks(), new ScryEffect(1));
    }
}
