package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;

public class UltimeciaOmnipotent extends Card {

    public UltimeciaOmnipotent() {
        addEffect(EffectSlot.ON_TRANSFORM_TO_BACK_FACE, new ControllerExtraTurnEffect(1));
    }
}
