package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BLB", collectorNumber = "82")
public class AgateBladeAssassin extends Card {

    public AgateBladeAssassin() {
        addTrigger(Triggers.attacks(),
                new LoseLifeEffect(1, Subjects.defendingPlayer()),
                new GainLifeEffect(1));
    }
}
