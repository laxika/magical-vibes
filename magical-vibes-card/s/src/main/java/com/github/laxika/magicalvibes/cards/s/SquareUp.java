package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;

@CardRegistration(set = "STX", collectorNumber = "238")
public class SquareUp extends Card {

    public SquareUp() {
        addSpellEffect(new SetBasePowerToughnessEffect(4, 4));
    }
}
