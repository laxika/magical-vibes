package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AnyPlayerMaySacrificeLandPutSourceOnTopEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "USG", collectorNumber = "236")
public class ArgothianWurm extends Card {

    public ArgothianWurm() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new AnyPlayerMaySacrificeLandPutSourceOnTopEffect());
    }
}
