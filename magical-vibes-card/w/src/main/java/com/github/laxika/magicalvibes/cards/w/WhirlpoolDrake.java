package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ShuffleHandIntoLibraryAndDrawEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "APC", collectorNumber = "34")
public class WhirlpoolDrake extends Card {

    public WhirlpoolDrake() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ShuffleHandIntoLibraryAndDrawEffect(false));
        addTrigger(Triggers.dies(ObjectRelation.THIS), new ShuffleHandIntoLibraryAndDrawEffect(false));
    }
}
