package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "13")
public class CompassionateHealer extends Card {

    public CompassionateHealer() {
        // Whenever this creature becomes tapped, you gain 1 life and scry 1.
        addTrigger(Triggers.becomesTapped(),
                new SequenceEffect(List.of(
                        new GainLifeEffect(1),
                        new ScryEffect(1)
                )));
    }
}
