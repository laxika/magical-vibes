package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SPM", collectorNumber = "90")
@CardRegistration(set = "OM1", collectorNumber = "97")
public class SpiderGwenFreeSpirit extends Card {

    public SpiderGwenFreeSpirit() {
        addTrigger(Triggers.becomesTapped(),
                new MayEffect(new DiscardAndDrawCardEffect(), "Discard a card to draw a card?"));
    }
}
