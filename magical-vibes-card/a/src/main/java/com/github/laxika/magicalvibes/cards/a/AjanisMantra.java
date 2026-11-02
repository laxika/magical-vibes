package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M11", collectorNumber = "2")
@CardRegistration(set = "DDH", collectorNumber = "22")
public class AjanisMantra extends Card {

    public AjanisMantra() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayEffect(
                new GainLifeEffect(1),
                "Gain 1 life?"
        ));
    }
}
