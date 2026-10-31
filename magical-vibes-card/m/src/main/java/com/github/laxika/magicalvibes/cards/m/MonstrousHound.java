package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerControlsMoreLandsThanOpponent;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockUnlessEffect;

@CardRegistration(set = "EXO", collectorNumber = "89")
public class MonstrousHound extends Card {

    public MonstrousHound() {
        addStaticEffect(new CantAttackUnlessEffect(
                new ControllerControlsMoreLandsThanOpponent(),
                "you control more lands than defending player"
        ));
        addStaticEffect(new CantBlockUnlessEffect(
                new ControllerControlsMoreLandsThanOpponent(),
                "you control more lands than attacking player"
        ));
    }
}
