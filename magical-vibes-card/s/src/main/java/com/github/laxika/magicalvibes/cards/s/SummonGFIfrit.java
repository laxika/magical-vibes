package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIN", collectorNumber = "163")
@CardRegistration(set = "FIN", collectorNumber = "369")
public class SummonGFIfrit extends Card {

    public SummonGFIfrit() {
        addTrigger(Triggers.chapter(1),
                new MayEffect(new DiscardAndDrawCardEffect(), "Discard a card to draw a card?"));
        addTrigger(Triggers.chapter(2),
                new MayEffect(new DiscardAndDrawCardEffect(), "Discard a card to draw a card?"));
        addTrigger(Triggers.chapter(3), new AwardManaEffect(ManaColor.RED));
        addTrigger(Triggers.chapter(4), new AwardManaEffect(ManaColor.RED));
    }
}
