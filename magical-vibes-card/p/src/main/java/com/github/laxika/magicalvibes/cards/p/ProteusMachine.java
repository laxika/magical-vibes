package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SourceBecomesChosenSubtypeIndefinitelyEffect;

@CardRegistration(set = "SCG", collectorNumber = "141")
public class ProteusMachine extends Card {

    public ProteusMachine() {
        addMorph("{0}");
        addEffect(EffectSlot.ON_TURNED_FACE_UP, new SourceBecomesChosenSubtypeIndefinitelyEffect());
    }
}
