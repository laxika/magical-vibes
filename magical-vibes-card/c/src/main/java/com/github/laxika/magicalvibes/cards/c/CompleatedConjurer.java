package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class CompleatedConjurer extends Card {

    public CompleatedConjurer() {
        addTrigger(Triggers.transformsToBackFace(ObjectRelation.THIS),
                new ExileTopCardsMayPlayUntilNextTurnEffect(1));
    }
}
