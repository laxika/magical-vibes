package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsMoreCreaturesThanOpponent;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockUnlessEffect;

@CardRegistration(set = "NEM", collectorNumber = "95")
public class MoggToady extends Card {

    public MoggToady() {
        ControlsMoreCreaturesThanOpponent condition = new ControlsMoreCreaturesThanOpponent();
        addEffect(EffectSlot.STATIC, new CantAttackUnlessEffect(
                condition,
                "you control more creatures than defending player"
        ));
        addEffect(EffectSlot.STATIC, new CantBlockUnlessEffect(
                condition,
                "you control more creatures than attacking player"
        ));
    }
}
