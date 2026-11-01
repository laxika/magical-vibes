package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class UltimeciaOmnipotent extends Card {

    public UltimeciaOmnipotent() {
        addTrigger(Triggers.transformsToBackFace(ObjectRelation.THIS), new ControllerExtraTurnEffect(1));
    }
}
