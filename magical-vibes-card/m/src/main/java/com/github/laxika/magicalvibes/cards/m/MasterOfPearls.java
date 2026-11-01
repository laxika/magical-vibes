package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KTK", collectorNumber = "18")
@CardRegistration(set = "MKC", collectorNumber = "73")
public class MasterOfPearls extends Card {

    public MasterOfPearls() {
        addMorph("{3}{W}{W}");
        addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.THIS), new BoostAllOwnCreaturesEffect(2, 2));
    }
}
