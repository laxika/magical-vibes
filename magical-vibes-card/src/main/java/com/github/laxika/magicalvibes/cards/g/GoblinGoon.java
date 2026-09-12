package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsMoreCreaturesThanOpponent;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockUnlessEffect;

@CardRegistration(set = "LGN", collectorNumber = "99")
@CardRegistration(set = "VMA", collectorNumber = "166")
public class GoblinGoon extends Card {

    public GoblinGoon() {
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
