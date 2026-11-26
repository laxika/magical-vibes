package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControlsMoreCreaturesThanOpponent;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessEffect;

@CardRegistration(set = "LGN", collectorNumber = "99")
@CardRegistration(set = "DDT", collectorNumber = "44")
@CardRegistration(set = "VMA", collectorNumber = "166")
public class GoblinGoon extends Card {

    public GoblinGoon() {
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
