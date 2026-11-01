package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.VentureIntoDungeonEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFR", collectorNumber = "93")
public class ClatteringSkeletons extends Card {

    public ClatteringSkeletons() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new VentureIntoDungeonEffect());
    }
}
