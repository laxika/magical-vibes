package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MBS", collectorNumber = "52")
public class PhyrexianVatmother extends Card {

    public PhyrexianVatmother() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new GivePoisonCountersEffect(1, PoisonRecipient.CONTROLLER));
    }
}
