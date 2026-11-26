package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControlsMoreCreaturesThanOpponent;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessEffect;

@CardRegistration(set = "NEM", collectorNumber = "95")
public class MoggToady extends Card {

    public MoggToady() {
        ControlsMoreCreaturesThanOpponent condition = new ControlsMoreCreaturesThanOpponent();
        addStaticEffect(CantAttackOrBlockUnlessEffect.attack(
                condition,
                "you control more creatures than defending player"
        ));
        addStaticEffect(CantAttackOrBlockUnlessEffect.block(
                condition,
                "you control more creatures than attacking player"
        ));
    }
}
