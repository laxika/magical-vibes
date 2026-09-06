package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PayXManaReanimateCreaturesWithTotalManaValueXEffect;

public class FireLordSozin extends Card {

    public FireLordSozin() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new PayXManaReanimateCreaturesWithTotalManaValueXEffect());
    }
}
