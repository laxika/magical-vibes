package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerControlsMoreLandsThanOpponent;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessEffect;

@CardRegistration(set = "EXO", collectorNumber = "89")
public class MonstrousHound extends Card {

    public MonstrousHound() {
        addStaticEffect(CantAttackOrBlockUnlessEffect.attack(
                new ControllerControlsMoreLandsThanOpponent(),
                "you control more lands than defending player"
        ));
        addStaticEffect(CantAttackOrBlockUnlessEffect.block(
                new ControllerControlsMoreLandsThanOpponent(),
                "you control more lands than attacking player"
        ));
    }
}
