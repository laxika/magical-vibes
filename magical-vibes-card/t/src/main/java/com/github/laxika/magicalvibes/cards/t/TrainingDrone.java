package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Equipped;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessEffect;

@CardRegistration(set = "MBS", collectorNumber = "142")
public class TrainingDrone extends Card {

    public TrainingDrone() {
        addEffect(EffectSlot.STATIC, new CantAttackOrBlockUnlessEffect(new Equipped(), "it's equipped"));
    }
}
