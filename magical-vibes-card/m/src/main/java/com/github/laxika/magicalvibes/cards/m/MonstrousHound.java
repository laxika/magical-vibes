package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerControlsMoreLandsThanOpponent;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockUnlessEffect;

@CardRegistration(set = "EXO", collectorNumber = "89")
public class MonstrousHound extends Card {

    public MonstrousHound() {
        addEffect(EffectSlot.STATIC, new CantAttackUnlessEffect(
                new ControllerControlsMoreLandsThanOpponent(),
                "you control more lands than defending player"
        ));
        addEffect(EffectSlot.STATIC, new CantBlockUnlessEffect(
                new ControllerControlsMoreLandsThanOpponent(),
                "you control more lands than attacking player"
        ));
    }
}
