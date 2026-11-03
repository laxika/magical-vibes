package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TDM", collectorNumber = "116")
public class RescueLeopard extends Card {

    public RescueLeopard() {
        addTrigger(Triggers.becomesTapped(),
                new MayEffect(new DiscardAndDrawCardEffect(), "Discard a card to draw a card?"));
    }
}
