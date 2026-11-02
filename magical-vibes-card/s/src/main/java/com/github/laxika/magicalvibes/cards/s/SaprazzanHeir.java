package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MMQ", collectorNumber = "99")
public class SaprazzanHeir extends Card {

    public SaprazzanHeir() {
        addTrigger(Triggers.becomesBlocked(),
                new MayEffect(new DrawCardEffect(3), "Draw three cards?"));
    }
}
