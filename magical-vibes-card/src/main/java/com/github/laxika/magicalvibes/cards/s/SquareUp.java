package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;

@CardRegistration(set = "STX", collectorNumber = "238")
public class SquareUp extends Card {

    public SquareUp() {
        addEffect(EffectSlot.SPELL, new SetBasePowerToughnessEffect(4, 4));
    }
}
