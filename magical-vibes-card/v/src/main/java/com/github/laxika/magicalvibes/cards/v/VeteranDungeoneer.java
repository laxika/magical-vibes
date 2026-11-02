package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.VentureIntoDungeonEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFR", collectorNumber = "40")
public class VeteranDungeoneer extends Card {

    public VeteranDungeoneer() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new VentureIntoDungeonEffect());
    }
}
