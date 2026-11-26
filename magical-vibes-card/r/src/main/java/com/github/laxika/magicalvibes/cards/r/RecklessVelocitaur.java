package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "DFT", collectorNumber = "144")
public class RecklessVelocitaur extends Card {

    public RecklessVelocitaur() {
        // "that Mount or Vehicle gets +2/+0 and gains trample until end of turn" (the crewed or saddled permanent).
        addTrigger(Triggers.saddlesOrCrewsDuringMainPhase(),
                BoostEffect.of(Subjects.triggering(), new Fixed(2), new Fixed(0), Set.of(Keyword.TRAMPLE)));
    }
}
