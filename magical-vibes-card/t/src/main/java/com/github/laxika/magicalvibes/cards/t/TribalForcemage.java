package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesOfChosenSubtypeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "LGN", collectorNumber = "142")
public class TribalForcemage extends Card {

    public TribalForcemage() {
        addMorph("{1}{G}");
        addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.THIS),
                new BoostAllCreaturesOfChosenSubtypeEffect(2, 2, Set.of(Keyword.TRAMPLE)));
    }
}
