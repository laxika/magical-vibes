package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFR", collectorNumber = "69")
@CardRegistration(set = "HBG", collectorNumber = "130")
@CardRegistration(set = "HBG", collectorNumber = "310")
public class RimeshieldFrostGiant extends Card {

    public RimeshieldFrostGiant() {
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(), new CounterUnlessPaysEffect(3));
    }
}
