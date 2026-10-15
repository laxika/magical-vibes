package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceAttackedDuringControllersLastTurn;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessEffect;

@CardRegistration(set = "LEG", collectorNumber = "188")
public class GiantTurtle extends Card {

    public GiantTurtle() {
        addEffect(EffectSlot.STATIC, new CantAttackUnlessEffect(
                new NotCondition(new SourceAttackedDuringControllersLastTurn()),
                "this creature did not attack during your last turn"
        ));
    }
}
